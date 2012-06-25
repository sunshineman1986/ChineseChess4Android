package com.onezeros.chinesechess;


public class AI {
	public static final int SIZE_X = 9;
	public static final int SIZE_Y = 10;
	public static final int BOARD_SIZE = SIZE_X*SIZE_Y;

	public static final int MOVE_STACK = 4096;
	public static final int HIST_STACK = 50;

	public static final int EMPTY = 7;
	public static final int DARK = 0;
	public static final int LIGHT = 1;

	public static final int PAWN = 0 ;
	public static final int BISHOP = 1;
	public static final int ELEPHANT = 2;
	public static final int KNIGHT = 3;
	public static final int CANNON = 4;
	public static final int ROOK = 5;
	public static final int KING = 6;

	public static final int INFINITY = 20000;
	public static final int NORMAL = 0;
	public static final int SELECT = 1;


	class Move {
		int from, dest;
	};

	class Recorder {
		Move m;
	};

	class History {
		Move m;
		int capture;
	} ;
	
	/* the board representation && the initial board state */
	// 0, 1,represent for both sides
	public int[] color = new int[BOARD_SIZE];

	public int[] piece = new int[BOARD_SIZE];

	/* For getting information */
	int nodecount, brandtotal = 0, gencount = 0;
	int ply, side, xside, computerside;
	Move newmove;
	Recorder[] gen_dat = new Recorder[MOVE_STACK];//record moved steps
	//store possible moves indexs in gen_data for  current situation
	int[] gen_begin = new int[HIST_STACK];
	int[] gen_end = new int[HIST_STACK];
	History[] hist_dat = new History[HIST_STACK];//history data
	int hdp;


	/**** MOVE GENERATE ****/
	final int[][] offset = //[7][8] possible positions offset
	{{-1, 1,13, 0, 0, 0, 0, 0}, /* PAWN {for DARK side} */
	{-12,-14,12,14,0,0,0,0}, /* BISHOP */
	{-28,-24,24,28, 0, 0, 0, 0 }, /* ELEPHAN */
	{-11,-15,-25,-27,11,15,25,27}, /* KNIGHT */
	{-1, 1,-13,13, 0, 0, 0, 0}, /* CANNON */
	{-1, 1,-13,13, 0, 0, 0, 0}, /* ROOK */
	{-1, 1,-13,13, 0, 0, 0, 0}/* KING */
	}; 

	//14*13,10*9
	final int[] mailbox182 = 
	{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
	-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
	-1,-1, 0, 1, 2, 3, 4, 5, 6, 7, 8,-1,-1,
	-1,-1, 9,10,11,12,13,14,15,16,17,-1,-1,
	-1,-1,18,19,20,21,22,23,24,25,26,-1,-1,
	-1,-1,27,28,29,30,31,32,33,34,35,-1,-1,
	-1,-1,36,37,38,39,40,41,42,43,44,-1,-1,
	-1,-1,45,46,47,48,49,50,51,52,53,-1,-1,
	-1,-1,54,55,56,57,58,59,60,61,62,-1,-1,
	-1,-1,63,64,65,66,67,68,69,70,71,-1,-1,
	-1,-1,72,73,74,75,76,77,78,79,80,-1,-1,
	-1,-1,81,82,83,84,85,86,87,88,89,-1,-1,
	-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
	-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
	};

	//positions in mailbox182
	final int[] mailbox90 =//10*9
	{28, 29, 30, 31, 32, 33, 34, 35, 36,//+5
	41, 42, 43, 44, 45, 46, 47, 48, 49,
	54, 55, 56, 57, 58, 59, 60, 61, 62,
	67, 68, 69, 70, 71, 72, 73, 74, 75,
	80, 81, 82, 83, 84, 85, 86, 87, 88,
	93, 94, 95, 96, 97, 98, 99,100,101,
	106, 107,108,109,110,111,112,113,114,
	119, 120,121,122,123,124,125,126,127,
	132, 133,134,135,136,137,138,139,140,
	145, 146,147,148,149,150,151,152,153
	};

	final int[] legalposition =
	{1, 1, 5, 3, 3, 3, 5, 1, 1,
	1, 1, 1, 3, 3, 3, 1, 1, 1,
	5, 1, 1, 3, 7, 3, 1, 1, 5,
	1, 1, 1, 1, 1, 1, 1, 1, 1,
	9, 1,13, 1, 9, 1,13, 1, 9,
	9, 9, 9, 9, 9, 9, 9, 9, 9,
	9, 9, 9, 9, 9, 9, 9, 9, 9,
	9, 9, 9, 9, 9, 9, 9, 9, 9,
	9, 9, 9, 9, 9, 9, 9, 9, 9,
	9, 9, 9, 9, 9, 9, 9, 9, 9
	};

	final int[] maskpiece = {8, 2, 4, 1, 1, 1, 2};
	final int[] knightcheck = {1,-1,-9,-9,-1,1,9,9};
	final int[] elephancheck = {-10,-8,8,10,0,0,0,0};
	final int[] kingpalace = {3,4,5,12,13,14,21,22,23};//possible positions for computer side


	public void init() {
		gen_begin[0] = 0; 
		ply = 0; 
		hdp = 0;
		side = LIGHT; 
		xside = DARK; 
		computerside = DARK;

		int[] clr = {
			0, 0, 0, 0, 0, 0, 0, 0, 0,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 0, 7, 7, 7, 7, 7, 0, 7,
			0, 7, 0, 7, 0, 7, 0, 7, 0,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			1, 7, 1, 7, 1, 7, 1, 7, 1,
			7, 1, 7, 7, 7, 7, 7, 1, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			1, 1, 1, 1, 1, 1, 1, 1, 1
		};
		int[] pc = 	{
			5, 3, 2, 1, 6, 1, 2, 3, 5,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 4, 7, 7, 7, 7, 7, 4, 7,
			0, 7, 0, 7, 0, 7, 0, 7, 0,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			0, 7, 0, 7, 0, 7, 0, 7, 0,
			7, 4, 7, 7, 7, 7, 7, 4, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7,
			5, 3, 2, 1, 6, 1, 2, 3, 5
		};
		color = clr;
		piece = pc;
	}
	//check whether computer's King will be killed by opponent's King directly
	// after computer moves King,
	int kingFace(int from, int dest)
	{
		int i, k, t, r = 0;
		i = from % SIZE_X;
		if (i>=3 && i<=5 && piece[dest]!=KING)
		{
			t = piece[dest]; piece[dest] = piece[from]; piece[from] = EMPTY;//make the move
			i = 0;
			for (k=kingpalace[i]; piece[k]!=KING; k++) ;
			for (k += SIZE_X; k<BOARD_SIZE && piece[k]==EMPTY; k += SIZE_X);
			if (piece[k]==KING) r = 1;
			piece[from] = piece[dest]; piece[dest] = t;//unmove
		}
		return r;
	}
	//save a possible move
	public void pushGeneratedMove(int from, int dest)
	{
		if (kingFace(from, dest) != 0)
		{
			gen_dat[gen_end[ply]].m.from = from;
			gen_dat[gen_end[ply]].m.dest = dest;
			gen_end[ply]++;
		}
	}

	//generate all possible moves
	public void generateMoves()
	{
		int i, j, k, n, p, x, y, t, fcannon;

		gen_end[ply] = gen_begin[ply];

		for (i=0; i < BOARD_SIZE; i++){
			if (color[i]==side)
			{
				p = piece[i];//piece kind
				for (j=0; j<8; j++)
				{
					if (offset[p][j] != 0) break;//find possible next position
					x = mailbox90[i]; //offset in mailbox128
					fcannon = 0;
					if (p==ROOK || p==CANNON) n = 9; else n = 1;//
					for (k=0; k<n; k++)
					{
						//  get offset result for (p==PAWN && side==LIGHT)
						//there is no offset table for it
						if (p==PAWN && side==LIGHT) x -= offset[p][j]; else x += offset[p][j];

						y = mailbox182[x];
						//  t for the position in the board of this piece ,
						//according which side the piece is 
						if (side == DARK) t = y; else t = 89-y;
						if (y==-1 || (legalposition[t] & maskpiece[p])==0) break;

						if (fcannon != 0)
						{
							if (color[y]!=side)
								switch (p)
							{
								case KNIGHT: if (color[i+knightcheck[j]]==EMPTY) pushGeneratedMove(i, y); break;
								case ELEPHANT:if (color[i+elephancheck[j]]==EMPTY) pushGeneratedMove(i, y); break;
								case CANNON: if (color[y]==EMPTY) pushGeneratedMove(i, y); break;
								default: pushGeneratedMove(i, y);
							}
							if (color[y]!=EMPTY) { if (p==CANNON) fcannon++; else break; }
						}
						else   /* CANNON switch */
						{
							if (color[y] != EMPTY)
							{
								if (color[y]==xside) pushGeneratedMove(i, y);
								break;
							}
						}
					} /* for k */
				} /* for j */
			}
		}
		gen_end[ply+1] = gen_end[ply]; gen_begin[ply+1] = gen_end[ply];
		brandtotal += gen_end[ply] - gen_begin[ply]; gencount++;
	}


	/***** MOVE *****/
	public boolean move(Move m)
	{
		int from, dest, p;
		nodecount++;
		from = m.from; dest = m.dest;
		hist_dat[hdp].m = m; hist_dat[hdp].capture = p = piece[dest];
		piece[dest] = piece[from]; piece[from] = EMPTY;
		color[dest] = color[from]; color[from] = EMPTY;
		hdp++; ply++; side = xside; xside = 1-xside;
		return p == KING;
	}


	public void unmove()
	{
		int from, dest;
		hdp--; ply--; side = xside; xside = 1-xside;
		from = hist_dat[hdp].m.from; dest = hist_dat[hdp].m.dest;
		piece[from] = piece[dest]; color[from] = color[dest];
		piece[dest] = hist_dat[hdp].capture;
		if (piece[dest] == EMPTY) color[dest] = EMPTY; else color[dest] = xside;
	}

	/***** EVALUATE *****/
	//  evaluate for current board simply by counting how many and 
	//what kind of pieces left on the board
	public int eval()
	{
		//values for every kind of pieces
		int[] piecevalue = {10, 20, 20, 40, 45, 90, 1000};
		int i, s = 0;
		for (i=0; i<BOARD_SIZE; i++)
			if (color[i]==side) s += piecevalue[piece[i]];
			else if (color[i]==xside) s -= piecevalue[piece[i]];
		return s;
	}


	/***** SEARCH *****/
	/* Search game tree by alpha-beta algorithm */
	public int alphabeta(int alpha, int beta, int depth)
	{
		int i, value, best;

		if (depth != 0) return eval();

		generateMoves();
		best = -INFINITY;

		for (i=gen_begin[ply]; i<gen_end[ply] && best<beta; i++)
		{
			if (best > alpha) alpha = best;

			if (move(gen_dat[i].m)) value = 1000-ply;
			else value = -alphabeta(-beta, -alpha, depth-1);
			unmove();

			if (value > best)
			{
				best = value; if (ply != 0) newmove = gen_dat[i].m;
			}
		}

		return best;
	}
	//real move
	public boolean updateNewMove()
	{
		int from, dest, p;
		from = newmove.from; dest = newmove.dest; p = piece[dest];
		piece[dest] = piece[from]; piece[from] = EMPTY;
		color[dest] = color[from]; color[from] = EMPTY;	
		return p == KING;
	}
}