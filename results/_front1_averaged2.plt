load '_settings.plt'

set key right bottom

set pointsize 1

front1_file_1 = 'stats/'.experiments.'-1-multi-size-cache/front1.txt'
front1_file_2 = 'stats/'.experiments.'-2-multi-size-cache/front1.txt'
front1_file_3 = 'stats/'.experiments.'-3-multi-size-cache/front1.txt'
front1_file_4 = 'stats/'.experiments.'-4-multi-size-cache/front1.txt'
front1_file_5 = 'stats/'.experiments.'-5-multi-size-cache/front1.txt'

frontB_file_1 = 'stats/'.experiments.'-1-multi-time-nocache/derived/front1_raw_scores.txt'
frontB_file_2 = 'stats/'.experiments.'-2-multi-time-nocache/derived/front1_raw_scores.txt'
frontB_file_3 = 'stats/'.experiments.'-3-multi-time-nocache/derived/front1_raw_scores.txt'
frontB_file_4 = 'stats/'.experiments.'-4-multi-time-nocache/derived/front1_raw_scores.txt'
frontB_file_5 = 'stats/'.experiments.'-5-multi-time-nocache/derived/front1_raw_scores.txt'


plot front1_file_1 u 3:2 t (experiments.'-1-'.kind) lt 1 pt 1, \
     front1_file_2 u 3:2 t (experiments.'-2-'.kind) lt 1 pt 2, \
     front1_file_3 u 3:2 t (experiments.'-3-'.kind) lt 1 pt 6, \
     front1_file_4 u 3:2 t (experiments.'-4-'.kind) lt 1 pt 4, \
     front1_file_5 u 3:2 t (experiments.'-5-'.kind) lt 1 pt 8, \
     frontB_file_1 u 6:3 t (experiments.'-1-'.kind) lt 1 pt 3, \
     frontB_file_2 u 6:3 t (experiments.'-2-'.kind) lt 1 pt 3, \
     frontB_file_3 u 6:3 t (experiments.'-3-'.kind) lt 1 pt 3, \
     frontB_file_4 u 6:3 t (experiments.'-4-'.kind) lt 1 pt 3, \
     frontB_file_5 u 6:3 t (experiments.'-5-'.kind) lt 1 pt 3
