load '_settings.plt'

set key right bottom

if (kind eq 'multi-time-nocache') set logscale x

set pointsize 1

front1_file_1 = 'stats/'.experiments.'-1-'.kind.'/front1.txt'
front1_file_2 = 'stats/'.experiments.'-2-'.kind.'/front1.txt'
front1_file_3 = 'stats/'.experiments.'-3-'.kind.'/front1.txt'
front1_file_4 = 'stats/'.experiments.'-4-'.kind.'/front1.txt'
front1_file_5 = 'stats/'.experiments.'-5-'.kind.'/front1.txt'


plot front1_file_1 u 3:2 t (experiments.'-1') lt 1 pt 1, \
     front1_file_2 u 3:2 t (experiments.'-2') lt 1 pt 2, \
     front1_file_3 u 3:2 t (experiments.'-3') lt 1 pt 6, \
     front1_file_4 u 3:2 t (experiments.'-4') lt 1 pt 4, \
     front1_file_5 u 3:2 t (experiments.'-5') lt 1 pt 8
