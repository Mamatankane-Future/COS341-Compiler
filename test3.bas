10 DIM M(7, 30)
20 f = 0
30 LET v5$ = ""
40 LET v10 = 0
50 LET v15 = 0
60 PRINT 2025
70 PRINT v10
80 INPUT "Please enter number: " v15
90 PRINT "Hello"
100 LET T1 = 2024
110 LET T2 = 2003
120 T0 = T1 - T2
130 LET v10 = T0
140 LET T3$ = "Future"
150 LET v5$ = T3$
160 PRINT v5$
170 M(1, f) = 11
180 M(2, f) = v10
190 M(3, f) = 2
200 GOSUB 320
210 REM LABEL L0
220 M(1, f) = 11
230 M(2, f) = v10
240 M(3, f) = 2
250 GOSUB 910
260 REM LABEL L1
270 LET T4 = M(0, f)
280 LET v10 = T4
290 PRINT v10
300 REM END
310 END
320 REM LABEL f146
330 f = f + 1
340 p149 = M(1, f - 1)
350 p152 = M(2, f - 1)
360 p155 = M(3, f - 1)
370 REM BEGIN
380 LET v163 = 0
390 LET v168 = 0
400 LET v173 = 0
410 LET T5 = p149
420 LET v10 = T5
430 LET T6 = p152
440 LET v163 = T6
450 LET T7 = p155
460 LET v173 = T7
470 M(1, f) = 11
480 M(2, f) = v168
490 M(3, f) = 2
500 GOSUB 560
510 REM LABEL L2
520 REM END
530 f = f - 1
540 RETURN
550 REM END
560 REM LABEL f237
570 f = f + 1
580 p240 = M(1, f - 1)
590 p243 = M(2, f - 1)
600 p246 = M(3, f - 1)
610 REM BEGIN
620 LET v254$ = ""
630 LET v259 = 0
640 LET v264 = 0
650 IF NOT p246 = 0 THEN GOTO 660 ELSE GOTO 790 FI
660 REM LABEL L3
670 LET T9 = p246
680 LET T10 = 1
690 T8 = T9 - T10
700 LET v259 = T8
710 PRINT v259
720 M(1, f) = p240
730 M(2, f) = p243
740 M(3, f) = v259
750 GOSUB 560
760 REM LABEL L6
770 REM END
780 GOTO 820
790 REM LABEL L4
800 PRINT 2025
810 REM END
820 REM LABEL L5
830 REM END
840 f = f - 1
850 RETURN
860 REM END
870 REM END
880 END
890 REM END
900 END
910 REM LABEL f375
920 f = f + 1
930 p378 = M(1, f - 1)
940 p381 = M(2, f - 1)
950 p384 = M(3, f - 1)
960 REM BEGIN
970 LET v392$ = ""
980 LET v397 = 0
990 LET v402 = 0
1000 LET T12 = 4
1010 T11 = SQR(T12)
1020 LET v402 = T11
1030 PRINT v402
1040 M(0, f - 1) = p381
1050 REM END
1060 f = f - 1
1070 RETURN
1080 REM END
1090 REM END
1100 END
1110 REM END