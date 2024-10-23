/* USEFUL INFO
* startGame() arguments
* type refers to the game type
* 0 = One Player
* 1 = CPU vs CPU
*
* mode refers to the difficulty
* 0 = very easy
* 1 = easy
* 2 = normal
* 3 = hard
* 4 = inhuman
*
* The speed variable determines how fast the ball moves
*
* The chain variable, every time the ball hits a paddle it subtracts speed by chain
* basically subtracting 1 each hit until it reaches 1ms, the limit.
*/

// custom graphics and colors for our paddles and ball
// this will change depending if the player chooses a preset or makes their own
var custom = {
  paddle : 'graphics/p_default.gif',
  ball : 'graphics/b_default.gif',
  id : 'ball',
  color : 'none'
}, paused = false, started = false; // helpful for checking if the game is paused or started.

// triggered by one of the many difficulty buttons
// did you know the type is determined by the modeList className ?
function startGame(type, mode) {
    if (started) return;
	started = true;
	
	// this hides a bunch of menus so they're not in the player's way when the game starts
	hide(getId('main-info'));
    for (var i=0, menus = document.getElementsByTagName('DIV'); i<menus.length; i++) if (/menu/.test(menus[i].className)) hide(menus[i]);
	  
  var wX = window.innerWidth, wY = window.innerHeight,
      scorep1 = 0, scorep2 = 0,
	  cap = cNum('cap',1),
      speed = cNum('speed',45),
	  chain = 0, best_chain = 0,
	  
	  ball = new Ball(),
      player1 = new Paddle('p1'),
      player2 = new Paddle('p2'),
	  controls_p1 = 'idle',
	  difficulty = [ 1,1 ], gameEnded = false;
	  
	  
  if (mode == 0) difficulty = [ 40,25 ]; // very easy
  if (mode == 1) difficulty = [ 30,20 ]; // easy
  if (mode == 2) difficulty = [ 20,15 ]; // normal
  if (mode == 3) difficulty = [ 10,1 ]; // hard
  if (mode == 4) difficulty = [ 3,1 ]; // inhuman
	  
  show(getId('UI')); // show UI
	  
  // set the coords of p1 and p2
  player1.setCoords(50,wY / 2 - 50);
  player2.setCoords(wX - 65, wY / 2 - 50);
  ball.reset(); // set the ball
  window.setTimeout(function() { ball.dir('random'), ball.animate(speed) },1000);
  
  // check game mode
  if (type == 0) initCPU(player2, difficulty[1]);
  if (type == 1) initCPU(player1, difficulty[1]), initCPU(player2, difficulty[1]);
  
  // paddle
  function Paddle(classname) {
    this.el = document.createElement('IMG');
	this.el.src = custom.paddle;
	if (custom.color != 'none') this.el.style.background = custom.color;
	this.el.className = 'paddle '+classname;
	this.setCoords = function(x,y) { this.el.style.left = Math.floor(x)+'px', this.el.style.top = Math.floor(y)+'px' };
	this.up = function() { this.el.style.top = getY(this.el) - 10 + 'px' };
	this.down = function() { this.el.style.top = getY(this.el) + 10 + 'px' };
	
	document.body.insertBefore(this.el,document.body.firstChild);
  };
  
  // ball
  function Ball() {
    this.el = document.createElement('IMG');
	this.el.src = custom.ball;
	if (custom.color != 'none') this.el.style.background = custom.color;
	this.el.id = custom.id;
	this.reset = function() { this.el.style.left = Math.floor(wX / 2)+'px', this.el.style.top = Math.floor(wY / 2)+'px' };
	this.dir = function(type) {
	  var r = Math.floor(Math.random() * 2), d;
	  if (type == 'random') {
	    if (r == 0) d = 'left_X10_Y0';
		if (r == 1) d = 'right_X10_Y0';
	  } else d = type;
	
	  this.el.className = d
	};
	
	// ball movement methods
    this.up = function() { this.el.style.top = getY(this.el) - sY() + 'px' };
	this.left = function() { this.el.style.left = getX(this.el) - sX() + 'px' };
	this.right = function() { this.el.style.left = getX(this.el) + sX() + 'px' };
	this.down = function() { this.el.style.top = getY(this.el) + sY() + 'px' };
	
	// ball animation
	this.animate = function(refresh) {
      var ball_animation = window.setInterval(function() {
	    if (gameEnded) return window.clearInterval(ball_animation);
	    if (paused) return;
        var chain_speed = 0, bX = getX(ball.el), bY = getY(ball.el);
	  
	    /* -- START window hitboxes -- */
	    if (bX < 0) return clear(), goal('p2', 'right_X10_Y0'); // left side is p1, so p2 gets +1
	    if (bX > wX - 15) return clear(), goal('p1', 'left_X10_Y0'); // right side is p2, so p1 gets +1
		
		// top / bottom right
		if (bY < 0 && ballDir('top-r')) sfx('collfx'), ball.dir('down-r_X10_Y'+sY());
		if (bY > wY - 15 && ballDir('down-r')) sfx('collfx'), ball.dir('top-r_X10_Y'+sY());
		
		// top / bottom left
		if (bY < 0 && ballDir('top-l')) sfx('collfx'), ball.dir('down-l_X10_Y'+sY());
		if (bY > wY - 15 && ballDir('down-l')) sfx('collfx'), ball.dir('top-l_X10_Y'+sY());
		/* -- END window hitboxes -- */
		
		
		/* -- START paddle hitboxes -- */
		hitbox(0, 10, 'top-r_X10_Y10', 'top-l_X10_Y10'); 
		hitbox(10, 20, 'top-r_X10_Y8', 'top-l_X10_Y8');
		hitbox(20, 30, 'top-r_X10_Y6', 'top-l_X10_Y6');
		hitbox(30, 40, 'top-r_X10_Y4', 'top-l_X10_Y4');
		hitbox(40, 50, 'top-r_X10_Y2', 'top-l_X10_Y2');
		hitbox(50, 60, 'top-r_X10_Y1', 'top-l_X10_Y1');
		hitbox(60, 70, 'down-r_X10_Y1', 'down-l_X10_Y1');
		hitbox(70, 80, 'down-r_X10_Y2', 'down-l_X10_Y2');
		hitbox(80, 90, 'down-r_X10_Y4', 'down-l_X10_Y4');
		hitbox(90, 100, 'down-r_X10_Y6', 'down-l_X10_Y6');
		hitbox(100, 110, 'down-r_X10_Y8', 'down-l_X10_Y8');
		hitbox(110, 120, 'down-r_X10_Y10', 'down-l_X10_Y10');
		/* -- END paddle hitboxes -- */
		
		// ball directions
	    if (ballDir('left')) ball.left();
	    if (ballDir('right')) ball.right();
		if (ballDir('top-r')) ball.right(), ball.up();
		if (ballDir('down-r')) ball.right(), ball.down();
		if (ballDir('top-l')) ball.left(), ball.up();
		if (ballDir('down-l')) ball.left(), ball.down();
		
		// generate hitboxes for the paddles
		// we want a hitbox of about 15px wide and 100px tall
		// start refers to the Y-offset we want to start at e.g. 0px
		// end refers to the Y-offset we want to stop at e.g. 40px
		// dir1 and dir2 will be used as arguments in hit(), check the conditions below ;)
		function hitbox(start, end, dir1, dir2) {
		  var incY = start;
          while (incY < end) {
	        var incX = 0;
	        while (incX < 16) {
			  // we subtract 15 from the top of our paddles as the ball is 15 tall
			  // this will allow us to set a somewhat accurate and larger hitbox
	          if (bY == getY(player1.el) - 15 + incY && bX == getX(player1.el) + incX) hit(dir1); // player1
			  if (bY == getY(player2.el) - 15 + incY && bX == getX(player2.el) - incX) hit(dir2); // player2
		      incX++
	        }
	        incY++
          }
		  // function to run when a hit is detected, the direction is different depending on the player
		  function hit(dir) {
		    chain += 1, incY = 999, incX = 999;
		    if (speed - chain > 1) chain_speed = speed - chain;
		    else chain_speed = 1;
			sfx('hitfx'), clear(), ball.dir(dir), ball.animate(chain_speed), syncUI();
		  }
		};
		function clear() { window.clearInterval(ball_animation) };
	  },refresh);
	}
	
	document.body.insertBefore(this.el,document.body.firstChild);
  };
  
  
  // key functions
  document.onkeydown = function keyMovement(e) {
    if (gameEnded) return; // completely ignore if the game is over
	
	// use key or keyCode depending on what's supported
	if (e.key) var keyId = e.key.toLowerCase().replace(/arrow/, ''), up = 'up', down = 'down', w = 'w', s = 's', p = 'p';
	else if (e.keyCode) var keyId = e.which || e.keyCode, up = 38, down = 40, w = 87, s = 83, p = 80;
	else return domAlert('Error','Sorry, no key identifiers are supported.','<div class="button" onclick="window.location.reload();">OK</div>');
  
    if (!paused && keyId == p) pause(), pD();
    else if (paused && keyId == p) resume(), pD();
  
    if (type == 1) return; // we ignore input if the mode is CPU vs CPU
	
	// up and down movement keys
	if (e.shiftKey) {
	  if (keyId == up || keyId == w) playerControl('up', true), pD();
	  if (keyId == down || keyId == s) playerControl('down', true), pD();
	} else {
	  if (keyId == up || keyId == w) playerControl('up', false), pD();
	  if (keyId == down || keyId == s) playerControl('down', false), pD();
	}
	function pD() { e.preventDefault() }
  };
  function stopPlayer() { controls_p1 = 'idle' }
  document.onkeyup = stopPlayer;
  
  // mobile control
  var controlDivUp = document.getElementById('controlUp');
  var controlDivDown = document.getElementById('controlDown');
  var controlDivTurbo = document.getElementById('controlTurbo')

  
  var isTurboDivHeld = false;
  controlDivUp.addEventListener('touchstart', function (e) {
    playerControl('up', isTurboDivHeld)
    e.preventDefault();
  });

  controlDivDown.addEventListener('touchstart', function (e) {
    playerControl('down', isTurboDivHeld)
    e.preventDefault();
  });

  document.addEventListener('touchcancel', stopPlayer);
  document.addEventListener('touchend', stopPlayer);

  controlDivTurbo.addEventListener('touchstart', function(e) { isTurboDivHeld = true; })
  controlDivTurbo.addEventListener('touchcancel', function(e) { isTurboDivHeld = false; })
  controlDivTurbo.addEventListener('touchend', function(e) { isTurboDivHeld = false; })

  // main movement of the player
  function playerControl(last, turbo) {
    if (controls_p1 == last) return;
    controls_p1 = last;
	move();

	// we use an interval so the controls are more responsive
	// without there's usually a delay while holding the button
    var controls = window.setInterval(function() {
      if (controls_p1 != last || paused) return window.clearInterval(controls);
      move();
	},turbo ? 1:cNum('sens',25));
	
	function move() {
	  if (controls_p1 == 'up') player1.up();
      if (controls_p1 == 'down') player1.down();
	   // adds some boundaries so the player doesn't move off screen
	  if (getY(player1.el) < 0) player1.setCoords(50,0);
	  if (getY(player1.el) > wY - 100) player1.setCoords(50,wY - 100);
	}
  };
  
  /* -- START CPU -- */
  function initCPU(o,r,nl,tl) {
    // the CPU will move between 0 and 100 to allow variation
	// additionally the refresh of the next interval is randomized
    var n = nl || 0, t = tl || 'inc', ref = Math.floor( Math.random() * difficulty[0] ) + difficulty[1], CPU = window.setInterval(function() {
	  if (gameEnded) return window.clearInterval(CPU);
	  if (paused) return;
      movement(o, n);
	  if (t=='inc') {
	    n++;
		if (n > 100) t = 'dec'
	  } else if (t=='dec') {
	    n--;
		if (n < 1) t = 'inc'
	  }
	},r);
	
	// movement of the CPU resides in this function
	// arguments are passed from the interval which contains the object and offset
	function movement(o,n) {
	  if (getY(ball.el) - n < getY(o.el)) o.up();
	  if (getY(ball.el) - n > getY(o.el)) o.down();
		
	  if (o==player2) {
	    if (getY(o.el) < 0) o.setCoords(wX - 65,0);
	    if (getY(o.el) > wY - 100) o.setCoords(wX - 65,wY - 100);
	  } else if (o==player1) {
	    if (getY(o.el) < 0) o.setCoords(50,0);
	    if (getY(o.el) > wY - 100) o.setCoords(50,wY - 100);
	  }
	};
	
	// to allow variation in the CPU
	// the speed is randomized every second
	window.setTimeout(function() {
	  window.clearInterval(CPU);
      initCPU(o, ref, n, t);
	},1000);
  }
  /* -- END CPU -- */
  
  // update various variables
  // it also updates the score in game
  function syncUI() {
    getId('p1').innerHTML = scorep1;
	getId('p2').innerHTML = scorep2;
	
	// chains
    var chainLevel = 'zeroChain';
	if (chain >= 1) chainLevel = 'goodChain';
	if (chain >= 25) chainLevel = 'greatChain';
	if (chain >= 50) chainLevel = 'superChain';
	getId('chain').className = chainLevel;
	getId('chain').innerHTML = chain;
	
	if (chain > best_chain) {
	  best_chain = chain;
	  getId('bestChain').innerHTML = best_chain;
	}
  };
  
  // triggered when the ball hits the left or right corner of the screen
  // adds to score, changes ball directions, and resets some stuff
  function goal(p,d) {
    if (p == 'p1') {
	  scorep1 += 1;
	  if (scorep1 >= cap) gameOver();
	}
	if (p == 'p2') {
	  scorep2 += 1;
	  if (scorep2 >= cap) gameOver();
	}
    chain = 0;
    sfx('goalfx'), ball.reset(), syncUI();
    setTimeout(function() { ball.dir(d), ball.animate(speed) },1000);
  };
  
  // game over
  // ends the game when the score cap has been reached
  // additionally it can be triggered from the pause menu
  function gameOver() {
  
    gameEnded = true;
	var winner, gameType, gameMode, p1w='', p2w='', name1, name2;
	
	// check type and set names
	if (type == 0) gameType = 'One Player', name1 = 'You', name2 = 'CPU';
	else gameType = 'CPU vs CPU', name1 = 'CPU1', name2 = 'CPU2';
	
	// check who won
	if (scorep1 > scorep2) winner = name1 + ' won !', p1w = 'winner';
	else if (scorep1 < scorep2) winner = name2 + ' won !', p2w = 'winner';
	else winner = 'Draw !'
	
	// check mode
	if (mode == 0) gameMode = 'Very Easy';
	if (mode == 1) gameMode = 'Easy';
	if (mode == 2) gameMode = 'Normal';
	if (mode == 3) gameMode = 'Hard';
	if (mode == 4) gameMode = 'Inhuman';
	
	// put together the statistics
	getId('gameWinner').innerHTML = winner;
	getId('gameType').innerHTML = '<span class="label">Game Type&nbsp;</span><span class="value">' + gameType + '</span>';
	getId('gameMode').innerHTML = '<span class="label">Difficulty&nbsp;</span><span class="value ' + gameMode.slice(0,1).toLowerCase() + gameMode.slice(1).replace(/\s/,'') + '">' + gameMode + '</span>';
	getId('gameScore1').innerHTML = '<span class="label '+ p1w +'">Player 1 Score&nbsp;</span><span class="value">' + scorep1 + '</span>';
	getId('gameScore2').innerHTML = '<span class="label '+ p2w +'">Player 2 Score&nbsp;</span><span class="value">' + scorep2 + '</span>';
	getId('maxChain').innerHTML = '<span class="label">Best Chain&nbsp;</span><span class="value">' + best_chain + '</span>';
	
    show(getId('gameOver'));
  };
  
  // triggered when quit game is selected from the pause menu
  // a domAlert() will display asking if the player really wants to quit
  getId('quitGame').onclick = function() {
    getId('confirmQuit').onclick = function() { gameOver(), hide(getId('popup')) }
  };
  
  // few helpers for coordinates and the ball direction
  function ballDir(d) { return new RegExp(d).test(ball.el.className) };
  function getX(el) { return Number(el.style.left.replace(/(%|px)/,'')) };
  function getY(el) { return Number(el.style.top.replace(/(%|px)/,'')) };
  function sX() { return Number(ball.el.className.replace(/.*?_X(\d+).*/,'$1')) }
  function sY() { return Number(ball.el.className.replace(/.*?_Y(\d+)/,'$1')) }
};


// pauses the game and shows the pause menu
function pause() {
  paused = true;
  show(getId('pause'));
};

// resumes the game and hits menus that may have been open
function resume() {
  paused = false;
  hide(getId('pause'), getId('customize'), getId('instructions'), getId('about'), getId('popup'));
};

// this updates the graphics of the game
// it's triggered from the customization menu presets
function setPreset(paddle, ball, id, color) {
  custom.paddle = paddle;
  custom.ball = ball;
  custom.id = id;
  custom.color = color;
  
  updatePreview(paddle, ball, color, id);
};

// set custom graphics and colors
function setCustom() {
  var paddle = getId('p_graphic').value, paddle = paddle.length > 0 ? paddle:'graphics/p_alpha.gif',
  ball = getId('b_graphic').value, ball = ball.length > 0 ? ball:'graphics/b_alpha.gif',
  color = getId('o_color').value, color = color.length > 0 ? color:'none',
  id = getId('b_spin').checked ? 'customBall':'ball';
  
  custom.paddle = paddle;
  custom.ball = ball;
  custom.id = id;
  custom.color = color;
  
  updatePreview(paddle, ball, color, id);
};

// updates the preview under customization
function updatePreview(paddle, ball, color, id) {
  var b = getId('ballP');
  b.src = ball, b.style.background = color, b.className = id;
  
  for (var i=0, img=document.getElementsByTagName('IMG'); i<img.length; i++) {
	if (/paddle/.test(img[i].className)) img[i].src = paddle, img[i].style.background = color;
	if (img[i].id == 'ball' || img[i].id == 'customBall') img[i].src = ball, img[i].style.background = color, img[i].id = id;
  }
};

// custom alert so we don't have to use those ugly browser alerts
// the args are pretty self explanatory
// the popup itself can be found in index.html just above this script
function domAlert(title, message, custom) {
  var OK = getId('OK'), cAlert = getId('customAlert');

  getId('popupTitle').innerHTML = title;
  getId('popupContent').innerHTML = message;
  if (custom) {
    hide(OK), show(cAlert);
    cAlert.innerHTML = custom;
  } else show(OK), hide(cAlert);
  
  show(getId('popup'));
};

function sfx(audio) { getId(audio).play() };
function getId(id) { return document.getElementById(id) };
function show() { for (var i=0,args=arguments; i<args.length; i++) args[i].style.display = '' };
function hide() { for (var i=0,args=arguments; i<args.length; i++) args[i].style.display = 'none' };
function modeType() { return Number(getId('modeList').className.replace(/mode_(\d+)/,'$1')) };
function cNum(id, def) { return Number(getId(id).value) > 0 ? Number(getId(id).value):def }