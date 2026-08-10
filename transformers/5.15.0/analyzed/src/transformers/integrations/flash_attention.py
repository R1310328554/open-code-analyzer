# Flash Attention 集成：FA2 前向封装，处理 dtype 修正与 MLA value 头维 padding。
import torch

from ..modeling_flash_attention_utils import _flash_attention_forward, flash_attn_supports_top_left_mask
from ..utils import logging


logger = logging.get_logger(__name__)

# _use_top_left_mask：当前 flash-attn 是否使用 top-left causal mask
_use_top_left_mask = flash_attn_supports_top_left_mask()


# get_target_dtype：query 为 float32 时推断 flash attention 目标 dtype
def get_target_dtype(query: torch.Tensor, module: torch.nn.Module) -> torch.dtype:
    """If the query is in float32, return a target dtype compatible with flash attention. Return None otherwise."""
    if query.dtype == torch.float32:
        device_type = query.device.type
        if torch.is_autocast_enabled(device_type):
            return torch.get_autocast_dtype(device_type)
        # Handle the case where the model is quantized
        elif hasattr(module.config, "_is_quantized"):
            return module.config.dtype
        else:
            return next(layer for layer in module.modules() if isinstance(layer, torch.nn.Linear)).weight.dtype
    return None


# flash_attention_forward：调用 _flash_attention_forward 的模型 attention 入口
def flash_attention_forward(
    module: torch.nn.Module,
    query: torch.Tensor,
    key: torch.Tensor,
    value: torch.Tensor,
    attention_mask: torch.Tensor | None,
    dropout: float = 0.0,
    scaling: float | None = None,
    sliding_window: int | None = None,
    softcap: float | None = None,
    is_causal: bool | None = None,
    s_aux: torch.Tensor | None = None,  # alias: learnable attention sink
    **kwargs,
) -> tuple[torch.Tensor, None]:
    if kwargs.get("output_attentions", False):
        logger.warning_once(
            "Flash Attention does not support `output_attentions=True`."
            " Please set your attention to `eager` if you want any of these features."
        )

    # This is before the transpose
    seq_len = query.shape[2]

    if any(dim == 0 for dim in query.shape):
        raise ValueError(
            "Tensor query has shape  with a zero dimension.\n"
            "FlashAttention does not support inputs with dim=0.\n"
            "Please check your input shapes or use SDPA instead."
        )
    # FA2 uses non-transposed inputs
    query = query.transpose(1, 2)
    key = key.transpose(1, 2)
    value = value.transpose(1, 2)

    # FlashAttention requires the query and value to share a head dim; pad `value` up to the
    # query head dim (e.g. MLA, where `v_head_dim < qk_head_dim`) and crop the output below.
    head_dim, v_head_dim = query.shape[-1], value.shape[-1]
    if v_head_dim != head_dim:
        value = torch.nn.functional.pad(value, [0, head_dim - v_head_dim])

    # In PEFT, usually we cast the layer norms in float32 for training stability reasons
    # therefore the input hidden states gets silently casted in float32. Hence, we need
    # cast them back in the correct dtype just to be sure everything works as expected.
    # This might slowdown training & inference so it is recommended to not cast the LayerNorms
    # in fp32. (usually our RMSNorm modules handle it correctly)
    target_dtype = get_target_dtype(query, module)

    # Instead of relying on the value set in the module directly, we use the is_causal passed in kwargs if it is presented
    is_causal = is_causal if is_causal is not None else module.is_causal

    attn_output = _flash_attention_forward(
        query,
        key,
        value,
        attention_mask,
        query_length=seq_len,
        is_causal=is_causal,
        dropout=dropout,
        softmax_scale=scaling,
        sliding_window=sliding_window,
        softcap=softcap,
        use_top_left_mask=_use_top_left_mask,
        target_dtype=target_dtype,
        attn_implementation=module.config._attn_implementation,
        layer_idx=module.layer_idx if hasattr(module, "layer_idx") else None,
        s_aux=(
            s_aux.to(query.dtype)  # FA only accepts half precision
            if s_aux is not None
            else None
        ),
        **kwargs,
    )

    if v_head_dim != head_dim:
        attn_output = attn_output[..., :v_head_dim]

    return attn_output, None
