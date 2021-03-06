package net.fishtron.apps.gpml;

import com.google.common.collect.Sets;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Sexpr;
import net.fishtron.utils.F;

import java.util.List;
import java.util.Set;

/**
 * Created by tom on 27.07.2017.
 */
public class SizeUtils {

    public static int workflowSize(AppTree appTree) {
        return workflowSize(appTree.toSexpr());
    }

    private static final int foldFactor = 5;

    private static final String stackingName = "stacking";

    private static final Set<String> methodNames = Sets.newHashSet("SVC" ,"logR" ,"gaussianNB" ,"DT" ,"Perceptron" ,"SGD" ,"PAC" ,"LDA" ,"QDA" ,"MLP" ,"PCA" ,"kBest" ,"kMeans");
    private static final Set<String> zeroLeafNames = Sets.newHashSet("nil", "copy" ,"vote", "booBegin", "booEnd", "stacker");

    private static final Set<String> opNames = Sets.newHashSet( "cons", "booster", "split", stackingName, "boosting");
    private static final Set<String> diaOpNames = Sets.newHashSet("dia" ,"dia0");


    private static int workflowSize(Sexpr tree) {
        String sym = tree.getSym();
        if (tree.isLeaf()) {
            if (methodNames.contains(sym))   {return 1;}
            if (zeroLeafNames.contains(sym)) {return 0;}
            throw new Error("Unsupported leaf symbol: "+sym);
        } else {
            List<Sexpr> args = tree.getArgs();
            if (opNames.contains(sym)) {
                return F.sumInt(F.map(args, SizeUtils::workflowSize));
            }
            if (diaOpNames.contains(sym)) {

                int numArgs = args.size();

                if (!(numArgs == 2 || numArgs == 3)) {
                    throw new Error("Unsupported number of args in sym "+sym+": "+numArgs);
                }

                Sexpr merger = args.get(numArgs-1);

                if (merger.getSym().equals(stackingName)) {

                    Sexpr splitPart = args.get(numArgs-2);

                    int sum = numArgs == 2 ? 0 : workflowSize(args.get(0));
                    sum += foldFactor * workflowSize(splitPart);
                    sum += workflowSize(merger);
                    return sum;

                } else {
                    return F.sumInt(F.map(args, SizeUtils::workflowSize));
                }

            }
            throw new Error("Unsupported operator symbol: "+sym);
        }
    }



}
