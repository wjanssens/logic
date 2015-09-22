function Model(value) {
	this.value = value;
};
Model.prototype.addClause = function(a, b, value) {
	this.value.clauses.add([a, b, value]);
};
Model.prototype.removeClause = function(a, b) {
	this.value.clauses.forEach(function(clause, index) {
		if ((clause[0] === a && clause[1] === b) ||
			(clause[0] === b && clause[1] === a)) {
			this.value.clauses.splice(index, 1);
		}
	});
};
Model.prototype.renameGroup(oldValue, newValue) {
	
};
Model.prototype.updateGroup(groupName, members) {
	
};
Model.prototype.draw = function(id) {
	var canvas = document.getElementById(id);
	var ctx = canvas.getContext('2d');
	ctx.textAlign = 'left';
	
	var problem = this.value.problem;
	var clauses = this.value.clauses;
	
	var groupNames = [];

	for (var groupName in problem) groupNames.push(groupName);

	
	// compute the total height
	var height = 100;
	var gy = 0;
	while (true) {
		var groupName = groupNames[gy];
		var group = problem[groupName];
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
		var groupName = groupNames[gx];
		var group = problem[groupName];
		var sz = group.length * 20;

		// top group name
		ctx.textAlign = 'center';
		ctx.fillText(groupName, x + sz/2, 8, sz);
		
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
		var ygroup = problem[groupName];
		var ysz = group.length * 20;
		
		var x = 100;
		// top: 1, 2 ... n
		for (var gx = 1; gx < groupNames.length; gx++) {
			var xgroupName = groupNames[gx];
			var xgroup = problem[groupName];
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
			
			for (var i in xgroup) {
				for (var j in ygroup) {
					var xitem = xgroup[i];
					var yitem = ygroup[i];

					// TODO
				}
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