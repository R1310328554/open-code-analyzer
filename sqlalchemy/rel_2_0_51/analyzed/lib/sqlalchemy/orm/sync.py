# orm/sync.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: allow-untyped-defs, allow-untyped-calls


"""private module containing functions used for copying data
between instances based on join conditions.

"""

# 关系同步工具：flush 时按 synchronize_pairs 复制/清空列值

from __future__ import annotations

from . import exc
from . import util as orm_util
from .base import PassiveFlag


# 将 source 实例列值复制到 dest（含 PK 级联标记）
def populate(
    source,
    source_mapper,
    dest,
    dest_mapper,
    synchronize_pairs,
    uowcommit,
    flag_cascaded_pks,
):
    source_dict = source.dict
    dest_dict = dest.dict

    for l, r in synchronize_pairs:
        try:
            # inline of source_mapper._get_state_attr_by_column
            prop = source_mapper._columntoproperty[l]
            value = source.manager[prop.key].impl.get(
                source, source_dict, PassiveFlag.PASSIVE_OFF
            )
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(False, source_mapper, l, dest_mapper, r, err)

        try:
            # inline of dest_mapper._set_state_attr_by_column
            prop = dest_mapper._columntoproperty[r]
            dest.manager[prop.key].impl.set(dest, dest_dict, value, None)
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(True, source_mapper, l, dest_mapper, r, err)

        # technically the "r.primary_key" check isn't
        # needed here, but we check for this condition to limit
        # how often this logic is invoked for memory/performance
        # reasons, since we only need this info for a primary key
        # destination.
        if (
            flag_cascaded_pks
            and l.primary_key
            and r.primary_key
            and r.references(l)
        ):
            uowcommit.attributes[("pk_cascaded", dest, r)] = True


# bulk insert 简化版 populate：仅操作 dict
def bulk_populate_inherit_keys(source_dict, source_mapper, synchronize_pairs):
    # a simplified version of populate() used by bulk insert mode
    for l, r in synchronize_pairs:
        try:
            prop = source_mapper._columntoproperty[l]
            value = source_dict[prop.key]
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(False, source_mapper, l, source_mapper, r, err)

        try:
            prop = source_mapper._columntoproperty[r]
            source_dict[prop.key] = value
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(True, source_mapper, l, source_mapper, r, err)


# 按 sync 规则将 dest 列置 NULL（禁止清空 PK）
def clear(dest, dest_mapper, synchronize_pairs):
    for l, r in synchronize_pairs:
        if (
            r.primary_key
            and dest_mapper._get_state_attr_by_column(dest, dest.dict, r)
            not in orm_util._none_set
        ):
            raise AssertionError(
                f"Dependency rule on column '{l}' "
                "tried to blank-out primary key "
                f"column '{r}' on instance '{orm_util.state_str(dest)}'"
            )
        try:
            dest_mapper._set_state_attr_by_column(dest, dest.dict, r, None)
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(True, None, l, dest_mapper, r, err)


# 写入新值并保留 old_prefix 前缀的旧值（UPDATE 参数）
def update(source, source_mapper, dest, old_prefix, synchronize_pairs):
    for l, r in synchronize_pairs:
        try:
            oldvalue = source_mapper._get_committed_attr_by_column(
                source.obj(), l
            )
            value = source_mapper._get_state_attr_by_column(
                source, source.dict, l, passive=PassiveFlag.PASSIVE_OFF
            )
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(False, source_mapper, l, None, r, err)
        dest[r.key] = value
        dest[old_prefix + r.key] = oldvalue


# 将 source 列值写入参数字典
def populate_dict(source, source_mapper, dict_, synchronize_pairs):
    for l, r in synchronize_pairs:
        try:
            value = source_mapper._get_state_attr_by_column(
                source, source.dict, l, passive=PassiveFlag.PASSIVE_OFF
            )
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(False, source_mapper, l, None, r, err)

        dict_[r.key] = value


# 判断 source 在 sync 列上是否有变更
def source_modified(uowcommit, source, source_mapper, synchronize_pairs):
    """return true if the source object has changes from an old to a
    new value on the given synchronize pairs

    """
    for l, r in synchronize_pairs:
        try:
            prop = source_mapper._columntoproperty[l]
        except exc.UnmappedColumnError as err:
            _raise_col_to_prop(False, source_mapper, l, None, r, err)
        history = uowcommit.get_attribute_history(
            source, prop.key, PassiveFlag.PASSIVE_NO_INITIALIZE
        )
        if bool(history.deleted):
            return True
    else:
        return False


def _raise_col_to_prop(
    isdest, source_mapper, source_column, dest_mapper, dest_column, err
):
    if isdest:
        raise exc.UnmappedColumnError(
            "Can't execute sync rule for "
            "destination column '%s'; mapper '%s' does not map "
            "this column.  Try using an explicit `foreign_keys` "
            "collection which does not include this column (or use "
            "a viewonly=True relation)." % (dest_column, dest_mapper)
        ) from err
    else:
        raise exc.UnmappedColumnError(
            "Can't execute sync rule for "
            "source column '%s'; mapper '%s' does not map this "
            "column.  Try using an explicit `foreign_keys` "
            "collection which does not include destination column "
            "'%s' (or use a viewonly=True relation)."
            % (source_column, source_mapper, dest_column)
        ) from err
