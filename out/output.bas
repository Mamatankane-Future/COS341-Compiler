10 DIM M(7, 30)
20 f = 0
30 LET v5 = 0
40 LET v10 = 0
50 PRINT "Factorial"
60 INPUT "Please enter number: " v10
70 M(1, f) = v10
80 M(2, f) = 0
90 M(3, f) = 0
100 GOSUB 170
110 REM LABEL L0
120 LET T0 = M(0, f)
130 LET v5 = T0
140 PRINT v5
150 REM END
160 END
170 REM LABEL f67
180 f = f + 1
190 p70 = M(1, f - 1)
200 p73 = M(2, f - 1)
210 p76 = M(3, f - 1)
220 REM BEGIN
230 LET v84 = 0
240 LET v89 = 0
250 LET v94 = 0
260 IF p70 = 1 THEN GOTO 270 ELSE GOTO 300 FI
270 REM LABEL L1
280 M(0, f - 1) = 1
290 REM END
300 REM LABEL L2
310 LET T2 = p70
320 LET T3 = 1
330 T1 = T2 - T3
340 LET v84 = T1
350 M(1, f) = v84
360 M(2, f) = 0
370 M(3, f) = 0
380 GOSUB 170
390 REM LABEL L3
400 LET T4 = M(0, f)
410 LET v89 = T4
420 LET T6 = v89
430 LET T7 = p70
440 T5 = T6 * T7
450 LET v94 = T5
460 M(0, f - 1) = v94
470 REM END
480 REM END
490 f = f - 1
500 RETURN
510 REM END
520 REM END
530 END
540 REM END
