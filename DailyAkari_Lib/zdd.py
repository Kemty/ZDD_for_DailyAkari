import sys
sys.setrecursionlimit(1000000)
from pyvis.network import Network
from collections import deque
from random import randrange
from sharedList import SharedList, SharedList_Node

"""
    組合せ集合は波括弧({})を省略して表記する.
    例: abc := {a, b, c}

    特に, 空の組合せ集合はλと表記する.
"""

class ZDD_Node:
    def __init__(self, zdd, top=None, zero=None, one=None):
        self.zdd = zdd
        self.top = top
        self.zero = zero
        self.one = one
        if top is not None:
            self.hash = (top, zero, one).__hash__()

    def __eq__(self, other) -> bool:
        if not isinstance(other, ZDD_Node): return False
        return self.top == other.top and self.zero is other.zero and self.one is other.one
    
    def __hash__(self) -> int:
        return self.hash
    
    def __repr__(self) -> str:
        ret = []
        stack = [(self, True)] # (現在のノード, 入りがけか？)
        current_set = []
        while stack:
            p, is_enter = stack.pop()
            if is_enter:
                if p is self.zdd.zero_terminal:
                    continue
                if p is self.zdd.one_terminal:
                    ret.append(current_set[:]) # current_setをコピーしてretに記録
                    continue
                current_set.append(p.top)
                stack.append((p.zero, True))
                stack.append((p, False))
                stack.append((p.one, True))
            elif p is not self.zdd.zero_terminal and p is not self.zdd.one_terminal:
                current_set.pop()
        return ret.__str__()
    
    def show(self, filename):
        """
            selfによるzddを図示
            filemame: html拡張子のファイル名
        """
        nodes = {self: 0}
        net = Network(directed=True)
        dq = deque()
        if self is self.zdd.zero_terminal:
            net.add_node(nodes[self], shape="box", label="⏊")
        elif self is self.zdd.one_terminal:
            net.add_node(nodes[self], shape="box", label="⏉")
        else:
            net.add_node(nodes[self], shape="circle", label=self.top.__repr__())
            dq.appendleft(self)
        while dq:
            p = dq.pop()
            if p.zero not in nodes:
                nodes[p.zero] = len(nodes)
                if p.zero is self.zdd.zero_terminal:
                    net.add_node(nodes[p.zero], shape="box", label="⏊")
                elif p.zero is self.zdd.one_terminal:
                    net.add_node(nodes[p.zero], shape="box", label="⏉")
                else:
                    net.add_node(nodes[p.zero], shape="circle", label=p.zero.top.__repr__())
                    dq.appendleft(p.zero)
            net.add_edge(nodes[p], nodes[p.zero], color="red")
            if p.one not in nodes:
                nodes[p.one] = len(nodes)
                if p.one is self.zdd.zero_terminal:
                    net.add_node(nodes[p.one], shape="box", label="⏊")
                elif p.one is self.zdd.one_terminal:
                    net.add_node(nodes[p.one], shape="box", label="⏉")
                else:
                    net.add_node(nodes[p.one], shape="circle", label=p.one.top.__repr__())
                    dq.appendleft(p.one)
            net.add_edge(nodes[p], nodes[p.one], color="blue")
        net.show(filename, notebook=False)

    def offset(self, e):
        """
            selfにおいて, eを含まない組合せ集合全体をなす部分zddを得る.
            例:
                self := {abc, ab, c}
                のとき
                self.offset(c) = {ab}
        """
        assert e in self.zdd.idx
        if self.top == e:
            return self.zero
        if self.zdd.idx[self.top] > self.zdd.idx[e]:
            return self
        key = (self.offset, self, e)
        if key not in self.zdd._cache_table:
            self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.offset(e), self.one.offset(e))
        return self.zdd._cache_table[key]
    
    def offset_s(self, E:SharedList):
        """
            selfにおいて, 各e∊Eを含まない組合せ集合全体をなす部分zddを得る.
            例:
                self := {ac, ab, ad, c}
                のとき
                self.offset({b, c}) = {ad}
        """
        if len(E) == 0 or self is self.zdd.zero_terminal or self is self.zdd.one_terminal:
            return self
        key = (self.offset_s, self, E)
        if key not in self.zdd._cache_table:
            e = E.top
            if self.top == e:
                self.zdd._cache_table[key] = self.zero.offset_s(E.nxt)
            elif self.zdd.idx[self.top] > self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.offset_s(E.nxt)
            else:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.offset_s(E), self.one.offset_s(E))
        return self.zdd._cache_table[key]

    def onset(self, e):
        """
            selfにおいて, eを含む各組合せ集合からeを除いて取り出したzddを得る.
            例:
                self := {abc, ab, c}
                のとき
                self.onset(c) = {ab, λ}
        """
        if self.zdd.idx[self.top] == self.zdd.idx[e]:
            return self.one
        if self.zdd.idx[self.top] > self.zdd.idx[e]:
            return self.zdd.zero_terminal
        key = (self.onset, self, e)
        if key not in self.zdd._cache_table:
            self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.onset(e), self.one.onset(e))
        return self.zdd._cache_table[key]
    
    def onset_s(self, E:SharedList_Node):
        """
            selfにおいて, 各e∊Eを含む各組合せ集合からeを除いて取り出したzddを得る.
            例:
                self := {abc, ab, c}
                のとき
                self.onset({b, c}) = {a}
        """
        if len(E) == 0 or self is self.zdd.zero_terminal:
            return self
        if self is self.zdd.one_terminal:
            return self.zdd.zero_terminal
        e = E.top
        if self.zdd.idx[self.top] > self.zdd.idx[e]:
            return self.zdd.zero_terminal
        key = (self.onset_s, self, E)
        if key not in self.zdd._cache_table:
            if self.top == e:
                self.zdd._cache_table[key] = self.one.onset_s(E.nxt)
            else:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.onset_s(E), self.one.onset_s(E))
        return self.zdd._cache_table[key]
    
    def __and__(self, other):
        """
            self ∩ other を表すzddを得る.
            例:
                self := {abc, ab, c}
                other := {ab, bc, λ}
                のとき
                self & other = {ab}
        """
        if self is self.zdd.zero_terminal or other is self.zdd.zero_terminal:
            return self.zdd.zero_terminal
        elif self is other:
            return self
        key = (self.__and__, self, other)
        if key not in self.zdd._cache_table:
            if self.zdd.idx[self.top] < self.zdd.idx[other.top]:
                self.zdd._cache_table[key] = self.zero & other
            elif self.zdd.idx[self.top] > self.zdd.idx[other.top]:
                self.zdd._cache_table[key] = self & other.zero
            else:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero & other.zero, self.one & other.one)
        return self.zdd._cache_table[key]
    
    def hint(self, E:SharedList, h:int):
        if len(E) < h:
            return self.zdd.zero_terminal
        elif h == 0:
            return self.offset_s(E)
        elif self is self.zdd.zero_terminal or self is self.zdd.one_terminal:
            return self.zdd.zero_terminal
        e = E.top
        key = (self.hint, self, E, h)
        if key not in self.zdd._cache_table:
            if self.zdd.idx[self.top] < self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.hint(E, h), self.one.hint(E, h))
            elif self.zdd.idx[self.top] > self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.hint(E.nxt, h)
            else:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.hint(E.nxt, h), self.one.hint(E.nxt, h-1))
        return self.zdd._cache_table[key]
    
    def hint_ge(self, E:SharedList, h:int):
        if len(E) < h:
            return self.zdd.zero_terminal
        elif h == 0:
            return self
        elif self is self.zdd.zero_terminal or self is self.zdd.one_terminal:
            return self.zdd.zero_terminal
        e = E.top
        key = (self.hint_ge, self, E, h)
        if key not in self.zdd._cache_table:
            if self.zdd.idx[self.top] < self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.hint_ge(E, h), self.one.hint_ge(E, h))
            elif self.zdd.idx[self.top] > self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.hint_ge(E.nxt, h)
            else:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.hint_ge(E.nxt, h), self.one.hint_ge(E.nxt, h-1))
        return self.zdd._cache_table[key]
    
    def hint_le(self, E:SharedList, h:int):
        if h == 0:
            return self.offset_s(E)
        elif len(E) <= h or self is self.zdd.zero_terminal or self is self.zdd.one_terminal:
            return self
        e = E.top
        key = (self.hint_le, self, E, h)
        if key not in self.zdd._cache_table:
            if self.zdd.idx[self.top] < self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.hint_le(E, h), self.one.hint_le(E, h))
            elif self.zdd.idx[self.top] > self.zdd.idx[e]:
                self.zdd._cache_table[key] = self.hint_le(E.nxt, h)
            else:
                self.zdd._cache_table[key] = self.zdd.get_node(self.top, self.zero.hint_le(E.nxt, h), self.one.hint_le(E.nxt, h-1))
        return self.zdd._cache_table[key]
    
    def count(self) -> int:
        """
            selfに含まれる組合せ集合の数を得る.
        """
        if self is self.zdd.zero_terminal:
            return 0
        elif self is self.zdd.one_terminal:
            return 1
        key = (self.count, self)
        if key not in self.zdd._cache_table:
            self.zdd._cache_table[key] = self.zero.count() + self.one.count()
        return self.zdd._cache_table[key]

class ZDD:
    def __init__(self, U):
        self.idx = {e: i for i, e in enumerate(U)}
        self.idx[None] = len(U) # 番兵として, 終端節点のラベルの添え字は最大に設定する
        self.zero_terminal = self._create_zero_terminal()
        self.one_terminal = self._create_one_terminal()
        self.node_table = {}
        self._cache_table = {}
        self.sl = SharedList(zdd=self)
        
        self.jump_cnt = 0 # 削除規則を適用した回数
        self.share_cnt = 0 # 共有規則を適用した回数
        self.create_cnt = 0 # 新しい節点を作った回数（終端節点は除く）
    
    def _create_zero_terminal(self):
        zero_terminal = ZDD_Node(zdd=self)
        zero_terminal.hash = randrange(10**9)
        return zero_terminal
    
    def _create_one_terminal(self):
        one_terminal = ZDD_Node(zdd=self)
        one_terminal.hash = randrange(10**9)
        while one_terminal.hash == self.zero_terminal.hash:
            one_terminal.hash = randrange(10**9)
        return one_terminal
        
    def get_node(self, e, zero, one):
        if one is self.zero_terminal:
            self.jump_cnt += 1
            return zero
        key = (e, zero, one)
        if key not in self.node_table:
            p = ZDD_Node(self, e, zero, one)
            self.node_table[key] = p
            self.create_cnt += 1
        else:
            p = self.node_table[key]
            self.share_cnt += 1
        return p
    
    def empty_set(self):
        """
            空のzdd ({}) を得る.
        """
        return self.zero_terminal
    
    def unit_set(self):
        """
            空の組合せ集合のみを持つzdd ({λ}) を得る.
        """
        return self.one_terminal

    def single_literal_set(self, e):
        """
            組合せ集合eのみを持つzdd ({e}) を得る.
        """
        assert e in self.idx
        return self.get_node(e, self.zero_terminal, self.one_terminal)
    
    def push(self, E:list|SharedList_Node):
        """
            組合せ集合Eのみを持つzdd({E})を得る.
        """
        node = self.one_terminal
        for e in sorted(E, key=lambda e: self.idx[e], reverse=True):
            node = self.get_node(e, self.zero_terminal, node)
        return node