#a = 1
#b = 1
#c = a + 2 * b
#return c

# const_1 $0
# const_1 $1
# mov $0 $2
# const_2 $3
# mul $3 $1
# add $2 $3
# mov $2 $0
# ret

#a = 1
#b = 1
#c = a + b * 2
#return c

# const_1 $0
# const_1 $1
# mov $0 $2
# mov $1 $3
# const_2 $4
# mul $3 $4
# add $2 $3
# mov $2 $0
# ret

a = 1
b = 1
return a + b * 2

# const_1 $0
# const_1 $1
# mov $1 $2
# const_2 $3
# mul $2 $3
# add $0 $2
# ret