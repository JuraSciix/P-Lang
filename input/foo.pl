i = 0
s = 0
while (i < 1_000_000) {
    i = i + 1
    if (i & 1 == 0) {
        # четное
        s = s + s * i
    } else {
        s = s - s * i
    }
}
return s
