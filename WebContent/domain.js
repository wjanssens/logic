Model = function(text) {
	var model = this;
	model.groups = {}; // map of group to elements
	model.pairs = []; // array of pair strings: element & element
	model.clauses = []; // array of clause strings: + element & element

	var lines = text.split('\n');

	// read in all the groups
	lines.forEach(function(line) {
		var found = line.match(/(.*?)\s*:\s*(.*)/); // group : element, element
		if (found) {
			model.groups[found[1]] = found[2].split(',').map(function(element) { return element.trim(); });
		}
	});

	// using the groups, create a list of all valid variable paris
	Object.keys(model.groups).forEach(function(groupA) {
		Object.keys(model.groups).forEach(function(groupB) {
			if (groupA == groupB) return; // elements from the same group cannot be a variable pair
			model.groups[groupA].forEach(function(elementA) {
				model.groups[groupB].forEach(function(elementB) {
					if (elementA == elementB) return; // don't combine same element pairs into a variable
					if (model.pairs.indexOf(elementA + ' & ' + elementB) >= 0
						|| model.pairs.indexOf(elementB + ' & ' + elementA) >= 0) {
						return;
					}
					model.pairs.push(elementA + ' & ' + elementB);
				});
			});
		});
	});

	// using the groups, create all of the derived clauses
	Object.keys(model.groups).forEach(function(groupA) {
		model.groups[groupA].forEach(function(elementA) {
			var outer = [];
			Object.keys(model.groups).forEach(function(groupB) {
				if (groupA == groupB) return; // elements from the same group cannot be a variable pair
				var inner = [];
				model.groups[groupB].forEach(function(elementB) {
					var variable = model.toVariable(elementA, elementB);
					outer.push(variable);
					inner.push(variable);
				});
				model.clauses.push(inner);
			});
			model.clauses.push(outer);
		});
	});

	// read in all the user clauses
	lines.forEach(function(line) {
		var found = line.match(/([+-])\s*(.*?)\s*&\s*(.*)/); // + element & element
		if (found) {
			model.clauses.push((found[1] == '-' ? -1 : +1) * model.toVariable(found[2], found[3]));
		}
	});
}
Model.prototype.toVariable = function(elementA, elementB) {
	var index = this.pairs.indexOf(elementA + ' & ' + elementB);
	if (index == -1) index = this.pairs.indexOf(elementB + ' & ' + elementA);
	if (index == -1) throw "Variable not found: " + elementA + ' & ' + elementB;
	return index + 1; // DIMACS variables are 1 based
}

/**
 * Returns a DIMACS input for consumption by minisat
 */
Model.prototype.dimacs = function() {
	var result = 'p cnf ' + this.pairs.length + ' ' + this.clauses.length;
	this.clauses.forEach(function(clause) {
		result += clause.join(' ');
		result += ' 0\n';
	});
}

/**
 * Interprets the DIMACS result produced by minisat
 * Returns an array of clause strings
 */
Model.prototype.result = function(text) {
	var result = [];
	text.split(' ').forEach(function(variable) {
		if ('SAT' == variable) return;
		var variable = parseInt(variable);
		if (variable < 0) {
			result.push('- ' + model.pairs[variable * -1]);
		}
		else {
			result.push('+ ' + model.pairs[variable]);
		}
	});
	return result;
}
