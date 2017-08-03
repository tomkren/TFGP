set key right bottom

allop_file=dir.'/'.allop.'_2-w'.window.'_averaged.txt'
xover_file=dir.'/'.xover.'_2-w'.window.'_averaged.txt'
mut_1_file=dir.'/'.mut_1.'_2-w'.window.'_averaged.txt'
mut_2_file=dir.'/'.mut_2.'_2-w'.window.'_averaged.txt'

plot allop_file u 1:2 t allop w l, \
     xover_file u 1:2 t xover w l, \
     mut_1_file u 1:2 t mut_1 w l, \
     mut_2_file u 1:2 t mut_2 w l
