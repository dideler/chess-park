
var Global = {};
Global.pickNum=1; // A move consists of 2 picks (1st square, 2nd square), a turn consists of 4 picks (1 move per player).
Global.oldSquare="";
Global.oldColour="";
Global.colourBoard=false;
Global.moveNum=0;
Global.moveLog=[];
Global.whitebg = "white";
Global.blackbg = "lightgrey";
SelectFrom = -1;

var Pieces=[];
Pieces[0]={html:"&nbsp;"};
// white pieces
/*// <---- Add a slash to the start of this line to use unicode instead of images
Pieces[1]={html:"&#9817;"}; // P
Pieces[2]={html:"&#9816;"}; // N
Pieces[3]={html:"&#9815;"}; // B
Pieces[4]={html:"&#9814;"}; // R
Pieces[5]={html:"&#9813;"}; // Q
Pieces[6]={html:"&#9812;"}; // K
// black pieces
Pieces[-1]={html:"&#9823;"}; // P
Pieces[-2]={html:"&#9822;"}; // N
Pieces[-3]={html:"&#9821;"}; // B
Pieces[-4]={html:"&#9820;"}; // R
Pieces[-5]={html:"&#9819;"}; // Q
Pieces[-6]={html:"&#9818;"}; // K
/*/
Pieces[1]={html:'<img src="images/wp.PNG" width="40" height="40" />'}; // P
Pieces[2]={html:'<img src="images/wn.PNG" width="40" height="40" />'}; // N
Pieces[3]={html:'<img src="images/wb.PNG" width="40" height="40" />'}; // B
Pieces[4]={html:'<img src="images/wr.PNG" width="40" height="40" />'}; // R
Pieces[5]={html:'<img src="images/wq.PNG" width="40" height="40" />'}; // Q
Pieces[6]={html:'<img src="images/wk.PNG" width="40" height="40" />'}; // K
// black pieces
Pieces[-1]={html:'<img src="images/bp.PNG" width="40" height="40" />'}; // P
Pieces[-2]={html:'<img src="images/bn.PNG" width="40" height="40" />'}; // N
Pieces[-3]={html:'<img src="images/bb.PNG" width="40" height="40" />'}; // B
Pieces[-4]={html:'<img src="images/br.PNG" width="40" height="40" />'}; // R
Pieces[-5]={html:'<img src="images/bq.PNG" width="40" height="40" />'}; // Q
Pieces[-6]={html:'<img src="images/bk.PNG" width="40" height="40" />'}; // K
//*/

var Board = [   0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0,
	            0, 0, 0, 0, 0, 0, 0, 0 ]


function f_drawBoard() {
	var board = document.getElementById('board');
	for (var r = 0; r < 8; ++r) {
		for (var c = 0; c < 8; ++c) {
			 board.rows[r].cells[c].innerHTML = Pieces[Board[r*8+c]].html; // R
		}
	}
}

function f_changeBoard() {
  Global.colourBoard =! Global.colourBoard;
  if (Global.colourBoard == true) {
    Global.whitebg = "#EED6AF";
    Global.blackbg = "#A67D3D";
  }
  else {
    Global.whitebg = "white";
    Global.blackbg = "lightgrey";
  }
  
  for (var r = 0; r < 8; r++) {
    for(var c = 0; c < 8; c++) {
      if ((r+c) % 2)
        board.rows[r].cells[c].style.backgroundColor = Global.blackbg;
      else
        board.rows[r].cells[c].style.backgroundColor = Global.whitebg;
		}
  }
}

function f_movePiece(from, dest) {
	Board[dest] = Board[from];
	Board[from] = 0;
}

function f_deselect() {
	if (SelectFrom != -1) {
		var board = document.getElementById('board');
		
		row = SelectFrom[0];
		col = SelectFrom[1];
		
		if ((row+col)%2) {
			board.rows[row].cells[col].style.backgroundColor = Global.blackbg; // dark
		} else {
			board.rows[row].cells[col].style.backgroundColor = Global.whitebg; // light
		}
		SelectFrom = -1;
	}
}
function f_select(row,col) {
	var board = document.getElementById('board');
	f_deselect();
	
	id = row*8+col;
	SelectFrom = [row,col];
	
  board.rows[row].cells[col].style.backgroundColor = "yellow";
}

function f_pick(square) {
	var board = document.getElementById('board');
	
	row = square[6]-0;
	col = square[7]-0;
	id = row*8+col;
	
	// no piece selected
	if (SelectFrom == -1) {
		//TODO validation
		f_select(row,col);
	}
	else {
		//TODO validating destination, changing selected piece
		from = SelectFrom[0]*8 + SelectFrom[1];
		dest = square;
		f_deselect();
		
		// move piece
		$.post("move",
			{ from: from, dest: id, table: Table.id },
			function(data){
			 if ($(data).find("move").size()) {
			  }
			  else if($(data).find("error").size()) {
				error = $(data).find("error").text();
				alert(error);
			  }
			  else if($(data).find("warning").size()) {
				warning = $(data).find("warning").text();
				alert(warning);
			  }
			},
			"xml");
	}
}
