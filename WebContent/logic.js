angular.module('Logic', ['ngMaterial'])
.config(function($mdThemingProvider) {
  $mdThemingProvider.theme('default')
    .primaryPalette('indigo')
    .accentPalette('pink');
}).controller('LogicCtrl', function($scope) {

	$scope.selectRules = function() {
		$scope.draw(document.getElementById('logic'));
	};

	$scope.definition = '';

	// TODO replace all references to this value to the new model object
	$scope.model = new Model(
		"Buyer: Glyn, Harry, Ian, Jamie, Kevin\n" +
		"CD: Caught Out, Friends, Our World, Wild Looks, Yellow Moon\n" +
		"Band: Girl Rock, The Goods, Headway, Hi Pitch, The Petals\n" +
		"+ Jamie  Headway\n" +
		"+ Harry & Our World\n" +
		"+ Caught Out & Hi Pitch\n" +
		"- Caught Out & Glyn\n" +
		"- Hi Pitch & Glyn\n" +
		"- Ian & The Petals\n" +
		"+ Ian & Wild Looks\n" +
		"+ Girl Rock & Yellow Moon"
	);

	$scope.draw = function(canvas) {
		var ctx = canvas.getContext('2d');
		ctx.textAlign = 'left';

		var groups = $scope.model.groups.keys();

		// compute the total height
		var height = 100;
		var gy = 0;
		while (true) {
			var ygroup = groups[gy];
			var ymembers = $scope.model.groups[ygroup];
			var ysz = ymembers.length * 20;
			height += ysz;

			if (gy == 2) break;
			else if (gy == 0) gy = groups.length - 1;
			else gy--;
		}

		// compute the total width
		var width = 100;
		for (var gx = 1; gx < groups.length; gx++) {
			var xgroup = groups[gx];
			var xmembers = $scope.model.groups[xgroup];
			var sz = xmembers.length * 20;
			width += sz;
		}

		canvas.width = width+1;
		canvas.height = height+1;

		//this.drawIcon(ctx, 5,5,12,12,[0,0,255,255],"BgBgBgBgBg////BgBgBgBgBg");

		// corner
		ctx.beginPath();
		ctx.moveTo(100,0);
		ctx.lineTo(100,100);
		ctx.lineTo(0,100);
		ctx.stroke();
		ctx.closePath();

		// side headers
		var y = 100;
		var gy = 0;
		while (true) {
			var ygroup = groups[gy];
			var ymembers = $scope.model.groups[ygroup];
			var ysz = ymembers.length * 20;

			// side group name
			ctx.save();
			ctx.translate(8, y + ysz / 2);
			ctx.rotate(-Math.PI / 2);
			ctx.textAlign = 'center';
			ctx.fillText(ygroup.name, 0, 0, sz);
			ctx.restore();

			// side group name line
			ctx.beginPath();
			ctx.moveTo(100, y);
			ctx.lineTo(100, y + ysz);
			ctx.lineTo(0, y + ysz);
			ctx.stroke();
			ctx.closePath();

			// side items
			ctx.textAlign = 'right';
			for (var i in ymembers) {
				ctx.fillText(ymembers[i], 95, 15 + y, 100);
				y += 20;
			}

			// side: 0, n ... 3, 2
			if (gy == 2) break;
			else if (gy == 0) gy = groups.length - 1;
			else gy--;
		}

		// top headers
		var x = 100;
		// top: 1, 2 ... n
		for (var gx = 1; gx < groups.length; gx++) {
			var xgroup = groups[gx];
			var xmembers = $scope.model.groups[xgroup];
			var xsz = xmembers.length * 20;

			// top group name
			ctx.textAlign = 'center';
			ctx.fillText(xgroup.name, x + xsz / 2, 8, xsz);

			// top group name line
			ctx.beginPath();
			ctx.moveTo(x + xsz, 0);
			ctx.lineTo(x + xsz, 100);
			ctx.lineTo(x, 100);
			ctx.stroke();
			ctx.closePath();

			// top items
			for (var i in xmembers) {
				ctx.save();
				ctx.translate(15 + x, 95);
				ctx.rotate(-Math.PI / 2);
				ctx.textAlign = 'left';
				ctx.fillText(xmembers[i], 0, 0, 100);
				ctx.restore();
				x += 20;
			}
		}

		// group combinations
		var y = 100;
		var gy = 0;
		while (true) {
			var ygroup = groups[gy];
			var ymembers = $scope.model.groups[ygroup];
			var ysz = ymembers.length * 20;

			var x = 100;
			// top: 1, 2 ... n
			for (var gx = 1; gx < groups.length; gx++) {
				var xgroup = groups[gx];
				var xmembers = $scope.model.groups[xgroup];
				var xsz = xmembers.length * 20;

				if (gy > 0 && gx > 1 && gx >= gy) continue;

				// vertical lines
				var dx = x;
				var dy = y;
				ctx.strokeStyle = '#ddd';
				for (var i in xmembers) {
					if (i == 0) continue;
					dx += 20;
					ctx.beginPath();
					ctx.moveTo(dx,dy)
					ctx.lineTo(dx,dy+ysz);
					ctx.stroke();
					ctx.closePath();
				}

				// horizontal lines
				var dx = x;
				var dy = y;
				for (var i in ymembers) {
					if (i == 0) continue;
					dy += 20
					ctx.beginPath();
					ctx.moveTo(dx,dy)
					ctx.lineTo(dx+xsz,dy);
					ctx.stroke();
					ctx.closePath();
				}

				// group combination corner
				ctx.strokeStyle = 'black';
				ctx.beginPath();
				ctx.moveTo(x+xsz,y);
				ctx.lineTo(x+xsz,y+ysz);
				ctx.lineTo(x,y+ysz);
				ctx.stroke();
				ctx.closePath();

				var dx = x;
				var clauses = this.value.clauses; // TODO
				for (var i in xmembers) {
					var dy = y;
					for (var j in ymembers) {
						var xitem = xgroup.name + "::" + xmembers[i];
						var yitem = ygroup.name + "::" + ymembers[j];

						var c0 = xitem + "||" + yitem;
						var c1 = yitem + "||" + xitem;
						if (clauses[c0] == true || clauses[c1] == true) {
							ctx.beginPath();
							ctx.arc(dx+10, dy+10, 8, 0, 2 * Math.PI, false);
							ctx.stroke();
							ctx.closePath();
						} else if (clauses[c0] == false || clauses[c1] == false) {
							ctx.beginPath();
							ctx.moveTo(dx+2, dy+2);
							ctx.lineTo(dx+18, dy+18);
							ctx.moveTo(dx+2, dy+18);
							ctx.lineTo(dx+18, dy+2);
							ctx.stroke();
							ctx.closePath();
						}
						dy += 20;
					}
					dx += 20;
				}

				x += xsz;
			}

			y += ysz;

			// side: 0, n ... 3, 2
			if (gy == 2) break;
			else if (gy == 0) gy = groups.length - 1;
			else gy--;
		}
	};

	$scope.click = function(evt) {
		var evtx = evt.offsetX;
		var evty = evt.offsetY;

		// group combinations
		var y = 100;
		var gy = 0;
		while (true) {
			var ygroup = groups[gy];
			var ymembers = $scope.model.groups[ygroup];
			var ysz = ymembers.length * 20;

			var x = 100;
			// top: 1, 2 ... n
			for (var gx = 1; gx < groups.length; gx++) {
				var xgroup = groups[gx];
				var xmembers = $scope.model.groups[xgroup];
				var xsz = xmembers.length * 20;

				if (gy > 0 && gx > 1 && gx >= gy) continue;

				var dx = x;
				for (var i in xmembers) {
					var dy = y;
					for (var j in ymembers) {
						if (evtx >= dx && evtx < dx + 20 && evty >= dy && evty < dy + 20) {
							var xitem = xgroup.name + "::" + xmembers[i];
							var yitem = ygroup.name + "::" + ymembers[j];

							var c0 = xitem + "||" + yitem;
							var c1 = yitem + "||" + xitem;

							if (clauses[c0] == true) {
								clauses[c0] = false;
							} else if (clauses[c1] == true) {
								clauses[c1] == false;
							} else if (clauses[c0] == false) {
								delete clauses[c0];
							} else if (clauses[c1] == false) {
								delete clauses[c1];
							} else {
								clauses[c0] = true;
							}

							$scope.draw(document.getElementById('logic'));
							return;
						}
						dy += 20;
					}
					dx += 20;
				}

				x += xsz;
			}

			y += ysz;

			// side: 0, n ... 3, 2
			if (gy == 2) break;
			else if (gy == 0) gy = groups.length - 1;
			else gy--;
		}
	};
});
