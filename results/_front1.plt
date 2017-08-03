load '_settings.plt'

set key right bottom

if (kind eq 'multi-time-nocache') set logscale x

set pointsize 1

front1_file = dir.'/front1.txt'
fitnes_file = dir.'/fitness2.txt'

plot front1_file u 3:2 t 'nondominated front' lt 1 pt 7
#    fitnes_file u 4:3 t 'all individuals' w points

