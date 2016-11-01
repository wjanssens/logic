function dimacs() {
	var result = 'p cnf ' + vars.length + ' ' + clauses.length;

	// vars is all of the valid combinations; i.e. "Buyer::Jamie", "CD::Caught Out", ...
	// clauses is all of the 
}


function solve() {
	var input = document.getElementById('input').value;
	var output = document.getElementById('output').value;
  var solve_string = Module.cwrap('solve_string', 'string', ['string', 'int']);
  try {
    var startTime = (new Date()).getTime();
    var result = solve_string(input, input.length);
    var endTime = (new Date()).getTime();
    output.value = result;
  } catch(e) {
    output.value = '';
  }
}
