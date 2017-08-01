set key right top

allop_file=dir.'/derived/'.allop.'_2-w'.window.'.txt'
xover_file=dir.'/derived/'.xover.'_2-w'.window.'.txt'
mut_1_file=dir.'/derived/'.mut_1.'_2-w'.window.'.txt'
mut_2_file=dir.'/derived/'.mut_2.'_2-w'.window.'.txt'

plot allop_file u 1:2 t allop w l, \
     xover_file u 1:2 t xover w l, \
     mut_1_file u 1:2 t mut_1 w l, \
     mut_2_file u 1:2 t mut_2 w l
