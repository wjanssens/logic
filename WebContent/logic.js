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
Model.prototype.encodeIcon = function(arr) {
	var str = String.fromCharCode.apply(null,a);
	return btoa(str);
};
Model.prototype.decodeIcon = function(str) {
	var result = [];
	var tmp = atob(str);
	for (var i = 0; i < tmp.length; i++) {
		result.push(tmp.charCodeAt(i));
	}
	return result;
}

Model.prototype.drawIcon = function(ctx, x, y, width, height, rgba, encoded) {
	var a = this.decodeIcon(encoded);
	var data = ctx.getImageData(x, y, width, height);

	//We need to figure out which bit the beginning of the character is, and how
	// many bytes are used for a glyph.
	var byteCount = ((width * height) >> 3); //(w*h)/8, int math
	var bitCount = (width * height) & 0x7; //(w*h)%8
	
	var bitCounter = 7;
	var byteCounter = 0;

	// account for padding, if any
	if (bitCount != 0) {
		byteCount++;
		bitCounter = bitCount - 1; // the padding is at the front of the first byte, so don't start at bit 0
	}
	var length = width * height * 4;
	for (var i = 0; i < length; i = i + 4) {
		if (a[byteCounter] & (1 << bitCounter)) {
			data[i+0] = rgba[0];
			data[i+1] = rgba[1];
			data[i+2] = rgba[2];
			data[i+3] = rgba[3];
		}
		if (bitCounter == 0){
			byteCounter++;
			bitCounter = 8;
		}
		bitCounter--;
	}
	ctx.putImageData(data,x,y);
};

Model.prototype.onclick = function(evt) {
	var evtx = evt.pageX - evt.target.offsetLeft;
	var evty = evt.pageY - evt.target.offsetTop;
	
	var problem = evt.data.value.problem;
	var clauses = evt.data.value.clauses;
	
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
						
						model.draw(this);
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
}


Model.prototype.draw = function(canvas) {
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
	
	this.drawIcon(ctx, 5,5,12,12,[0,0,255,255],"BgBgBgBgBg////BgBgBgBgBg");

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