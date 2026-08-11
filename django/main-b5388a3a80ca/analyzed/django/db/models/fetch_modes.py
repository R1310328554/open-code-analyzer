"""
django.db.models.fetch_modes — 延迟字段的取值策略。

控制 prefetch/defer 场景下访问字段时是单取、批量取 peers 还是禁止。
"""
from django.core.exceptions import FieldFetchBlocked


# 字段取值策略抽象基类
class FetchMode:from django.core.exceptions import FieldFetchBlocked


class FetchMode:
    __slots__ = ()

    track_peers = False

    def fetch(self, fetcher, instance):
        raise NotImplementedError("Subclasses must implement this method.")


# 仅对当前实例调用 fetcher.fetch_one
class FetchOne(FetchMode):
    __slots__ = ()

    def fetch(self, fetcher, instance):
        fetcher.fetch_one(instance)

    def __reduce__(self):
        return "FETCH_ONE"


FETCH_ONE = FetchOne()


# 若存在多个 peer 实例则 fetch_many，否则 fetch_one
class FetchPeers(FetchMode):
    __slots__ = ()

    track_peers = True

    def fetch(self, fetcher, instance):
        instances = [
            peer
            for peer_weakref in instance._state.peers
            if (peer := peer_weakref()) is not None
        ]
        if len(instances) > 1:
            fetcher.fetch_many(instances)
        else:
            fetcher.fetch_one(instance)

    def __reduce__(self):
        return "FETCH_PEERS"


FETCH_PEERS = FetchPeers()


# 禁止取值，抛出 FieldFetchBlocked
class FetchRaise(FetchMode):
    __slots__ = ()

    def fetch(self, fetcher, instance):
        klass = instance.__class__.__qualname__
        field_name = fetcher.field.name
        raise FieldFetchBlocked(f"Fetching of {klass}.{field_name} blocked.") from None

    def __reduce__(self):
        return "FETCH_RAISE"


FETCH_RAISE = FetchRaise()
