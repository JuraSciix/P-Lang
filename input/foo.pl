// load 1 $0
// mul $0 $0 $1
// add $0 $1 $1
// div $0 $0 $2
// sub $1 $2 $1
// ret $1

a = 1;
return (a + (a * a)) - (a / a);