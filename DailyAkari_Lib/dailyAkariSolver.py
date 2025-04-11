from dailyAkari import DailyAkari
from zdd import ZDD, ZDD_Node

class DailyAkariSolver:

    D = [(1, 0), (-1, 0), (0, 1), (0, -1)]

    def __init__(self, dailyAkari:DailyAkari):
        self.probrem = dailyAkari
    
    def solve(self):
        zdd = ZDD(list(reversed(range(self.probrem.H*self.probrem.W))))
        ans = zdd.unit_set()
        
        # build power set
        for i in range(self.probrem.H):
            for j in range(self.probrem.W):
                if self.probrem.board[i][j] == ".":
                    ans = zdd.get_node(self._cast_liner(i, j), ans, ans)

        # apply hint operations
        for i in range(self.probrem.H):
            for j in range(self.probrem.W):
                x = self.probrem.board[i][j]
                if x == ".":
                    E = self._get_cross(i, j)
                    ans = ans.hint_ge(zdd.sl.push(E), 1)
                else:
                    E = self._get_down(i, j)
                    ans = ans.hint_le(zdd.sl.push(E), 1)
                    E = self._get_right(i, j)
                    ans = ans.hint_le(zdd.sl.push(E), 1)
                    if x != "#":
                        E = self._get_neighbour(i, j)
                        ans = ans.hint(zdd.sl.push(E), int(x))
        for i in range(self.probrem.H):
            E = self._get_right(i, -1)
            ans = ans.hint_le(zdd.sl.push(E), 1)
        for j in range(self.probrem.W):
            E = self._get_down(-1, j)
            ans = ans.hint_le(zdd.sl.push(E), 1)
        
        # set answer to board
        self._set_answer(ans)

        return ans

    def _cast_liner(self, i, j):
        return i*self.probrem.W + j
    
    def _cast_2d(self, e):
        return divmod(e, self.probrem.W)
    
    def _does_it_in_board(self, i, j):
        return 0 <= i < self.probrem.H and 0 <= j < self.probrem.W
    
    def _get_cross(self, i, j):
        E = [self._cast_liner(i, j)]
        for di, dj in self.D:
            ni, nj = i+di, j+dj
            while self._does_it_in_board(ni, nj) and board[ni][nj] == ".":
                E.append(self._cast_liner(ni, nj))
                ni += di
                nj += dj
        return E
    
    def _get_down(self, i, j):
        E = []
        di, dj = 1, 0
        ni, nj = i+di, j+dj
        while self._does_it_in_board(ni, nj) and board[ni][nj] == ".":
            E.append(self._cast_liner(ni, nj))
            ni += di
            nj += dj
        return E
    
    def _get_right(self, i, j):
        E = []
        di, dj = 0, 1
        ni, nj = i+di, j+dj
        while self._does_it_in_board(ni, nj) and board[ni][nj] == ".":
            E.append(self._cast_liner(ni, nj))
            ni += di
            nj += dj
        return E

    def _get_neighbour(self, i, j):
        E = []
        for di, dj in self.D:
            ni, nj = i+di, j+dj
            if self._does_it_in_board(ni, nj) and board[ni][nj] == ".":
                E.append(self._cast_liner(ni, nj))
        return E
    
    def _set_answer(self, ans:ZDD_Node):
        node = ans
        while node is not node.zdd.one_terminal:
            i, j = self._cast_2d(node.top)
            self.probrem.change_akari(i, j)
            node = node.one
         

if __name__ == "__main__":
    H, W = map(int, input().split())
    board = [list(input()) for _ in range(H)]
    
    dailyAkari = DailyAkari(H, W, board)
    solver = DailyAkariSolver(dailyAkari)
    solver.solve().show("check.html")
    solver.probrem.view_board()

"""
9 9
.........
.1.1.2.2.
.........
.2.#.#.1.
...#.#...
.2.#.#.2.
.........
.1.1.1.2.
.........

"""