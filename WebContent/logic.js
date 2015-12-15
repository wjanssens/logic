function Group(name, members) {
	this.name = name;
	this.members = members;
}
Group.prototype.memberNames = function(members) {
	if (arguments.length) {
		// setter
		this.members = members.split('\n');
	} else {
		// getter
		return this.members.join('\n');
	}
}

function Problem(problem) {
	this.groups = [];
	for (var k in problem) {
		this.groups.push(new Group(k,problem[k]));
	}
}
Problem.prototype.groupNames = function(groups) {
	if (arguments.length) {
		// setter
		var split = groups.split('\n');
		while (this.groups.length > split.length) this.groups.pop();
		for (var i in split) {
			if (this.groups.length <= i) {
				this.groups.push(new Group(split[i], []));
			} else {
				this.groups[i].name = split[i];
			}
		}
	} else {
		// getter
		var result = '';
		for (var i in this.groups) {
			result += this.groups[i].name;
			result += '\n';
		}
		return result;
	}
}
Problem.prototype.toJson = function() {
	// TODO
}

function Model(value) {
	this.problem = new Problem(value.problem);
	//this.clauses = new Clauses(value.clauses);
	//this.solution = new Clauses(value.solution);
}


angular.module('Logic', ['ngMaterial'])
.controller('LogicCtrl', function($scope) {
	
	$scope.selectRules = function() {
		$scope.draw(document.getElementById('logic'));
	};
	
	// TODO replace all references to this value to the new model object
	$scope.value = {
		"problem": {
			"Buyer": [
				"Glyn", "Harry", "Ian", "Jamie", "Kevin"
			],
			"CD": [
				"Caught Out", "Friends", "Our World", "Wild Looks", "Yellow Moon"
			],
			"Band": [
				"Girl Rock", "The Goods", "Headway", "Hi Pitch", "The Petals"
			]
		},
		"clauses": {
			"Buyer::Jamie||Band::Headway": true,
			"Buyer::Harry||CD::Our World": true,
			"CD::Caught Out||Band::Hi Pitch": true,
			"CD::Caught Out||Buyer::Glyn": false,
			"Band::Hi Pitch||Buyer::Glyn": false,
			"Buyer::Ian||CD::Wild Looks": true,
			"Buyer::Ian||Band::The Petals": false,
			"Band::Girl Rock||CD::Yellow Moon": true
		}
	};
	$scope.model = new Model($scope.value);
	
	$scope.draw = function(canvas) {
		var ctx = canvas.getContext('2d');
		ctx.textAlign = 'left';
		
		var problem = $scope.value.problem;
		var clauses = $scope.value.clauses;
		
		var groupNames = [];
		for (var groupName in problem) groupNames.push(groupName);
		
		// compute the total height
		var height = 100;
		var gy = 0;
		while (true) {
			var ygroupName = groupNames[gy];
			var group = problem[ygroupName];
			var sz = group.length * 20;
			height += sz;
			
			if (gy == 2) break;
			else if (gy == 0) gy = groupNames.length - 1;
			else gy--;
		}
		
		// compute the total width
		var width = 100;
		for (var gx = 1; gx < groupNames.length; gx++) {
			var groupName = groupNames[gx];
			var group = problem[groupName];
			var sz = group.length * 20;
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
			var groupName = groupNames[gy];
			var group = problem[groupName];
			var sz = group.length * 20;
			
			// side group name
			ctx.save();
			ctx.translate(8, y + sz/2);
			ctx.rotate(-Math.PI / 2);
			ctx.textAlign = 'center';
			ctx.fillText(groupName, 0, 0, sz);
			ctx.restore();
			
			// side group name line
			ctx.beginPath();
			ctx.moveTo(100,y);
			ctx.lineTo(100,y+sz);
			ctx.lineTo(0,y+sz);
			ctx.stroke();
			ctx.closePath();

			// side items
			ctx.textAlign = 'right';
			for (var i in group) {
				var item = group[i];
				ctx.fillText(item, 95, 15+y, 100);
				y += 20;
			}

			// side: 0, n ... 3, 2
			if (gy == 2) break;
			else if (gy == 0) gy = groupNames.length - 1;
			else gy--;
		}
		
		// top headers
		var x = 100;
		// top: 1, 2 ... n
		for (var gx = 1; gx < groupNames.length; gx++) {
			var xgroupName = groupNames[gx];
			var xgroup = problem[xgroupName];
			var sz = xgroup.length * 20;

			// top group name
			ctx.textAlign = 'center';
			ctx.fillText(xgroupName, x + sz/2, 8, sz);
			
			// top group name line
			ctx.beginPath();
			ctx.moveTo(x+sz,0);
			ctx.lineTo(x+sz,100);
			ctx.lineTo(x,100);
			ctx.stroke();
			ctx.closePath();

			// top items
			for (var i in group) {
				var item = group[i];
				ctx.save();
				ctx.translate(15+x, 95);
				ctx.rotate(-Math.PI / 2);
				ctx.textAlign = 'left';
				ctx.fillText(item, 0, 0, 100);
				ctx.restore();
				x += 20;
			}
		}
		
		// group combinations
		var y = 100;
		var gy = 0;
		while (true) {
			var ygroupName = groupNames[gy];
			var ygroup = problem[ygroupName];
			var ysz = ygroup.length * 20;
			
			var x = 100;
			// top: 1, 2 ... n
			for (var gx = 1; gx < groupNames.length; gx++) {
				var xgroupName = groupNames[gx];
				var xgroup = problem[xgroupName];
				var xsz = group.length * 20;

				if (gy > 0 && gx > 1 && gx >= gy) continue;
				
				// vertical lines
				var dx = x;
				var dy = y;
				ctx.strokeStyle = '#ddd';
				for (var i in xgroup) {
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
				for (var i in ygroup) {
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
				var clauses = this.value.clauses;
				for (var i in xgroup) {
					var dy = y;
					for (var j in ygroup) {
						var xitem = xgroupName + "::" + xgroup[i];
						var yitem = ygroupName + "::" + ygroup[j];
						
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
			else if (gy == 0) gy = groupNames.length - 1;
			else gy--;
		}
	};
	
	$scope.click = function(evt) {
		var evtx = evt.pageX - evt.target.offsetLeft;
		var evty = evt.pageY - evt.target.offsetTop;
		
		var problem = $scope.value.problem;
		var clauses = $scope.value.clauses;
		
		var groupNames = [];
		for (var groupName in problem) groupNames.push(groupName);

		// group combinations
		var y = 100;
		var gy = 0;
		while (true) {
			var ygroupName = groupNames[gy];
			var ygroup = problem[ygroupName];
			var ysz = ygroup.length * 20;
			
			var x = 100;
			// top: 1, 2 ... n
			for (var gx = 1; gx < groupNames.length; gx++) {
				var xgroupName = groupNames[gx];
				var xgroup = problem[xgroupName];
				var xsz = xgroup.length * 20;

				if (gy > 0 && gx > 1 && gx >= gy) continue;
				
				var dx = x;
				for (var i in xgroup) {
					var dy = y;
					for (var j in ygroup) {
						if (evtx >= dx && evtx < dx + 20 && evty >= dy && evty < dy + 20) {
							var xitem = xgroupName + "::" + xgroup[i];
							var yitem = ygroupName + "::" + ygroup[j];
							
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
			else if (gy == 0) gy = groupNames.length - 1;
			else gy--;
		}
	};
});
