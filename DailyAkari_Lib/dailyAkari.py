class DailyAkari:
    
    def __init__(self, H, W, board):
        self.H = H
        self.W = W
        self.board = [list(row) for row in board]
    
    def _does_it_in_board(self, i, j):
        return 0 <= i < self.H and 0 <= j < self.W
    
    def change_akari(self, i, j):
        if not self._does_it_in_board(i, j):
            return False
        x = self.board[i][j]
        if x == ".":
            self.board[i][j] = "@"
        elif x == "@":
            self.board[i][j] = "."
        else:
            return False
        return True
    
    def view_board(self):
        print("current board:")
        for row in self.board:
            print("".join(row))