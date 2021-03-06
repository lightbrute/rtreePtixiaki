/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.github.davidmoten.rtree.geometry.Geometries.point;
//to create substrings
import org.apache.commons.lang3.StringUtils;


public class App {

    private static List<Entry<String, Point>> entrySort(List<Entry<String, Point>> list){ //sorts a List of Entries based on x Value
        list.sort((e1, e2) -> { //original list.sort(new Comparator<Entry<String, Point>>()
            Point point1 = e1.geometry();
            double xValue1 = point1.x();

            Point point2 = e2.geometry();
            double xValue2 = point2.x();

            return (int) (xValue1 - xValue2);

        });

        return list;
    }


    //Plane Sweep Algorithm for Spatial Join Returning list of Pairs
    private static List<Pairs> pairsPlaneSweep(List<Entry<String, Point>> list1, List<Entry<String, Point>> list2, double maxDist){

        double euclideanDistance;
        double xValue1;
        double yValue1;
        double xValue2;
        double yValue2;
        Entry<String, Point> referencePoint1;
        Entry<String, Point> referencePoint2;
        List<Pairs> myPairs = new ArrayList<>();

        //Step 1: Sort the Lists
        entrySort(list1);
        entrySort(list2);

        //Step 2: Set Reference Points/distance/...

        for (Entry<String, Point> aList1 : list1) {
            referencePoint1 = aList1;
            Point point1 = referencePoint1.geometry();
            xValue1 = point1.x();
            yValue1 = point1.y();


            for (Entry<String, Point> aList2 : list2) {
                referencePoint2 = aList2;
                Point point2 = referencePoint2.geometry();
                xValue2 = point2.x();
                yValue2 = point2.y();

                euclideanDistance = Math.sqrt((xValue1 - xValue2) * (xValue1 - xValue2) + (yValue1 - yValue2) * (yValue1 - yValue2));

                //Step 3: Find Pairs
                if (xValue2 - xValue1 > maxDist) { //break out of the loop
                    break;
                } else if (euclideanDistance > maxDist) { //do nothing
                } else {
                    myPairs.add(new Pairs(referencePoint1, referencePoint2));
                }

            }

        }

        return myPairs;

    }

    //Brute Force Algorithm for Spatial Join
    public static List<Entry<String, Point>> bruteForceSJ(List<Entry<String, Point>> list1,List<Entry<String, Point>> list2, double maxDist){

        double euclideanDistance;
        double xValue1;
        double yValue1;
        double xValue2;
        double yValue2;
        Entry<String, Point> referencePoint1;
        Entry<String, Point> referencePoint2;


        //Step 1: Set Reference Points/distance/...
        List<Entry<String, Point>> pairList = new ArrayList<>();

        for (int i=0; i<list1.size(); i++){
            referencePoint1 = list1.get(i);
            Point point1 = referencePoint1.geometry();
            xValue1 = point1.x();
            yValue1 = point1.y();


            for (int j=0; j<list2.size(); j++){
                referencePoint2 = list2.get(j);
                Point point2 = referencePoint2.geometry();
                xValue2 = point2.x();
                yValue2 = point2.y();

                euclideanDistance = Math.sqrt((xValue1 - xValue2)*(xValue1 - xValue2)+(yValue1 - yValue2)*(yValue1 - yValue2));

                //Step 2: Find Pairs
                if (euclideanDistance <= maxDist) {
                    pairList.add(referencePoint1);
                    pairList.add(referencePoint2);
                }

            }

        }

        return pairList;

    }

    //Read RTree Data from File
    private static RTree<String, Point> readRTreeFromFile(RTree<String, Point> tree, String fileName) {

        // This will reference one line at a time
        String line;
        String toAddValue;
        String toAddX;
        String toAddY;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                toAddValue = StringUtils.substringBetween(line, "e=", ", g"); //get subString
                toAddX = StringUtils.substringBetween(line, "x=", ", y");
                toAddY = StringUtils.substringBetween(line, " y=", "]");
                if (toAddValue != null) { //IF IS NEEDED TO AVOID NULL VALUES
                   // System.out.println(toAddValue + " " + toAddX + " " + toAddY);
                    tree = tree.add(toAddValue, point(Double.parseDouble(toAddX),Double.parseDouble(toAddY)));
                }

            }

            // Close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }


        return tree;


    }

    //Plane Sweep Algorithm for Spatial Join Returning list of Pairs PLUS Jaccard!!!
    private static List<Pairs> planeSweepJaccard(List<Entry<String, Point>> list1, List<Entry<String, Point>> list2, double maxDist) {

        double euclideanDistance;
        double xValue1;
        double yValue1;
        double xValue2;
        double yValue2;
        double similarity; //for jaccard
        Entry<String, Point> referencePoint1;
        Entry<String, Point> referencePoint2;
        List<Pairs> myPairs = new ArrayList<>();

        //Step 1: Sort the Lists
        entrySort(list1);
        entrySort(list2);

        //Step 2: Set Reference Points/distance/...

        for (Entry<String, Point> aList1 : list1) {
            referencePoint1 = aList1;
            Point point1 = referencePoint1.geometry();
            xValue1 = point1.x();
            yValue1 = point1.y();


            for (Entry<String, Point> aList2 : list2) {
                referencePoint2 = aList2;
                Point point2 = referencePoint2.geometry();
                xValue2 = point2.x();
                yValue2 = point2.y();

                euclideanDistance = Math.sqrt((xValue1 - xValue2) * (xValue1 - xValue2) + (yValue1 - yValue2) * (yValue1 - yValue2));

                //Step 3: Find Pairs
                if (xValue2 - xValue1 > maxDist) { //break out of the loop
                    break;
                } else if (euclideanDistance > maxDist) { //do nothing
                } else {

                    Pairs myPair = new Pairs(referencePoint1, referencePoint2);
                    similarity = Pairs.pairJaccardSimilarity(myPair);

                    //Step 4: Add ONLY if text is similar
                    if (similarity > 0.2) {
                        myPairs.add(myPair);
                    }

                }

            }

        }
        return myPairs;

    }

    private String getGreeting() {
        return "START";
    }


    public static void main(String[] args) throws IOException {
        System.out.println(new App().getGreeting());


        RTree<String, Point> tree1 = RTree.maxChildren(5).minChildren(4).create();
        RTree<String, Point> tree2 = RTree.maxChildren(5).minChildren(4).create();



        tree1 = readRTreeFromFile(tree1, "src/main/java/resources/data1.txt");

        tree2 = readRTreeFromFile(tree2, "src/main/java/resources/data2.txt");


        //Get entries
        Observable<Entry<String, Point>> tree1Entries = tree1.entries();

        Observable<Entry<String, Point>> tree2Entries = tree2.entries();

        //From observable to List
        List<Entry<String, Point>> entryList1 = tree1Entries.toList().toBlocking().single();

        List<Entry<String, Point>> entryList2 = tree2Entries.toList().toBlocking().single();



        //Display Ordered Lists
        System.out.println("------");
        System.out.println("List 1 Ordered by x Value:");

        System.out.println(entrySort(entryList1));


        System.out.println("------");
        System.out.println("List 2 Ordered by x Value:");

        System.out.println(entrySort(entryList2));

        System.out.println("------");



        //Plane Sweep Spatial Join WITH Jaccard Textual Join
        List<Pairs> listOfPairsONLYSPATIAL = pairsPlaneSweep(entryList1, entryList2, 30);
        List<Pairs> listOfPairs = planeSweepJaccard(entryList1, entryList2, 30);

        System.out.println("After Spatial Join (Plane Sweep):");

        System.out.println(listOfPairsONLYSPATIAL);

        System.out.println("------");

        System.out.println("After Spatial AND Textual Join (Plane Sweep & Jaccard): ");
        System.out.println(listOfPairs);


        //BRUTE FORCE O(N^2) CHECK

//        System.out.println("------");
//        System.out.println("Brute Force Check O(n^2)");
//        bruteForceSJ(entryList1, entryList2,30);


        System.out.println("------");
        System.out.println("END");


    }



}
