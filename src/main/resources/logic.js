function Group(name, members) {
        this.name = name;
        this.members = members;
}

function Problem(problem) {
        this.groups = [];
        for (var k in problem) {
            this.groups.push(new Group(k,problem[k]));
        }
}

Problem.prototype.newGroup = function(name) {
    return new Group(name,[]);
}

function Model(value) {
        this.problem = new Problem(value.dimensions);
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
                "dimensions": {
                    "Friend": [ "Judy", "Michael", "Norma", "Robin" ],
                    "Gift": [ "Book", "Chocolates", "Painting", "Vase" ],
                    "Paper": [ "Blue", "Shiny", "White", "Yellow" ],
                    "Bow": [ "Green", "Orange", "Pink", "Red" ]
                },
                "cnf": [
                    "Vase == Yellow",
                    "Vase != Judy",
                    "Judy == Orange",
                    "Shiny == Michael",
                    "Michael != Book",
                    "Painting == Norma",
                    "White == Red",
                    "Robin != Pink",
                    "Judy != Yellow",
                    "Orange != Yellow"
                ],
        };
        $scope.model = new Model($scope.value);

        $scope.draw = function(canvas) {
                var ctx = canvas.getContext('2d');
                ctx.textAlign = 'left';

                var dimensions = $scope.value.dimensions;
                var cnf = $scope.value.cnf;

                var groupNames = [];
                for (var groupName in dimensions) groupNames.push(groupName);

                // compute the total height
                var height = 100;
                var gy = 0;
                while (true) {
                        var ygroupName = groupNames[gy];
                        var group = dimensions[ygroupName];
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
                        var group = dimensions[groupName];
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
                        var group = dimensions[groupName];
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
                        var xgroup = dimensions[xgroupName];
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
                        var ygroup = dimensions[ygroupName];
                        var ysz = ygroup.length * 20;

                        var x = 100;
                        // top: 1, 2 ... n
                        for (var gx = 1; gx < groupNames.length; gx++) {
                                var xgroupName = groupNames[gx];
                                var xgroup = dimensions[xgroupName];
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
                                var clauses = this.value.cnf;
                                for (var i in xgroup) {
                                        var dy = y;
                                        for (var j in ygroup) {
                                                var xitem = xgroup[i];
                                                var yitem = ygroup[j];

                                                var pc0 = xitem + "==" + yitem;
                                                var pc1 = yitem + "==" + xitem;
                                                var nc0 = xitem + "!=" + yitem;
                                                var nc1 = yitem + "!=" + xitem;
                                                var ipc0 = clauses.indexOf(pc0);
                                                var ipc1 = clauses.indexOf(pc1);
                                                var inc0 = clauses.indexOf(nc0);
                                                var inc1 = clauses.indexOf(nc1);
                                                if (ipc0 >= 0 || ipc1 >= 0) {
                                                        ctx.beginPath();
                                                        ctx.arc(dx+10, dy+10, 8, 0, 2 * Math.PI, false);
                                                        ctx.stroke();
                                                        ctx.closePath();
                                                } else if (inc0 >= 0 || inc1 >= 0) {
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
                var evtx = evt.offsetX;
                var evty = evt.offsetY;

                var dimensions = $scope.value.dimensions;
                var clauses = $scope.value.cnf;

                var groupNames = [];
                for (var groupName in dimensions) groupNames.push(groupName);

                // group combinations
                var y = 100;
                var gy = 0;
                while (true) {
                        var ygroupName = groupNames[gy];
                        var ygroup = dimensions[ygroupName];
                        var ysz = ygroup.length * 20;

                        var x = 100;
                        // top: 1, 2 ... n
                        for (var gx = 1; gx < groupNames.length; gx++) {
                                var xgroupName = groupNames[gx];
                                var xgroup = dimensions[xgroupName];
                                var xsz = xgroup.length * 20;

                                if (gy > 0 && gx > 1 && gx >= gy) continue;

                                var dx = x;
                                for (var i in xgroup) {
                                        var dy = y;
                                        for (var j in ygroup) {
                                                if (evtx >= dx && evtx < dx + 20 && evty >= dy && evty < dy + 20) {
                                                        var xitem = xgroup[i];
                                                        var yitem = ygroup[j];

                                                        var pc0 = xitem + "==" + yitem;
                                                        var pc1 = yitem + "==" + xitem;
                                                        var nc0 = xitem + "!=" + yitem;
                                                        var nc1 = yitem + "!=" + xitem;
                                                        var ipc0 = clauses.indexOf(pc0);
                                                        var ipc1 = clauses.indexOf(pc1);
                                                        var inc0 = clauses.indexOf(nc0);
                                                        var inc1 = clauses.indexOf(nc1);

                                                        if (ipc0 >= 0) {
                                                                clauses[ipc0] = nc0; // positive to negative
                                                        } else if (ipc1 >= 0) {
                                                                clauses[ipc1] = nc0; // positive to negative
                                                        } else if (inc0 >= 0) {
                                                                delete clauses[inc0]; // negative to nothing
                                                        } else if (inc1 >= 0) {
                                                                delete clauses[inc1]; // negative to nothing
                                                        } else {
                                                                clauses.push(pc0); // nothing to positive
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