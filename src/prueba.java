import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class prueba {

    public static void main(String[] args) throws Exception {

        String csv_source_path = "Suicide_Detection_raw.csv";
        String arff_origin_path = "Suicide_Detection.arff";

        String[] instantzien_lista = getInstantzienLista(csv_source_path);
        String[][] instantzienMatrizea = getInstantzienMatrizea(instantzien_lista);

        arffGoiburuaEzarri(instantzien_lista, arff_origin_path);
        gordeInstantzienMatrizea(instantzienMatrizea, arff_origin_path);
    }

    public static void arffGoiburuaEzarri (String[] instantzien_lista, String path) throws IOException {

        FileWriter myWriter = new FileWriter(path);
        myWriter.append("@relation Suicide_Detection");

        String [] atributuen_lista = instantzien_lista[0].split("\r?\n|\r");
        atributuen_lista = atributuen_lista[0].split(",");

        myWriter.append("\n" +"@attribute " + atributuen_lista[1] + " string");
        myWriter.append("\n" +"@attribute " + atributuen_lista[2] + " {'suicide', 'non-suicide'}");

        myWriter.close();

    }

    public static void gordeInstantzienMatrizea(String[][] instantzien_matrizea, String path) throws IOException {

        FileWriter myWriter = new FileWriter(path, true);
        myWriter.append("\n");
        myWriter.append("@data");
        for (int i = 0; i < instantzien_matrizea.length; i++){

            myWriter.append("\n"+ instantzien_matrizea[i][1] +" " + instantzien_matrizea[i][2]);
        }

        myWriter.close();

    }
    public static String[][] getInstantzienMatrizea(String[] lista) {
        String[][] ema = new String[lista.length][3];
        for (int i = 0; i < lista.length; i++) {
            String unekoa = lista[i];
            String[] l = unekoa.split(",");
            ema[i][0] = l[0];
            ema[i][2] = "\'"+l[l.length - 1].replace("\n", "")+"\'";
            StringBuilder erdikoa = new StringBuilder();
            for (int j = 1; j <= l.length - 2; j++) {
                erdikoa.append(l[j]).append(" ");
            }
            String testua = removePunctuations(erdikoa.toString());
            ema[i][1] = "\""+testua.replace("\r\n", " ").replace("\n", " ").replace("\r", " ").replace("\"", "")+"\",";

            //System.out.println("\nID: " + ema[i][0] + "\nTestua: " + ema[i][1] + "\nKlasea: " + ema[i][2] + "\n-------------------------------------------------------------------------------");
        }
        System.out.println("Hasierako matrizearen luzeera: "+ema.length);
        return garbituInstantzienMatrizea(ema);
    }

    //TODO
    public static String[][] garbituInstantzienMatrizea(String[][] mat) {
        String[][] aux = new String[mat.length][mat[0].length];
        int i=0;
        for(String[] instantzia: mat){
            if(instantzia[2].equals("\'suicide\'") || instantzia[2].equals("\'non-suicide\'")){
                if(70<instantzia[1].length() && instantzia[1].length()<1500){
                    if(instantzia[1].contains("\s")){
                        aux[i]=instantzia;
                        i++;
                    }
                }else {System.out.println(instantzia[2]);}
            }
        }
        String[][] ema = new String[i][aux[0].length];
        for(int j=0; j<i; j++){
            ema[j]=aux[j];
        }
        System.out.println("Instantziak kenduz "+ema.length);
        return ema;
    }
    public static String[] getInstantzienLista(String path) throws IOException {
        Path csvPath = Path.of(path);
        String csvEdukia = Files.readString(csvPath);
        String[] instantzien_lista = csvEdukia.split("(?<=suicide\n)");
        //String[] instantzien_lista = csvEdukia.split("(?<=,non\\-suicide\n)|(?<=,suicide\n)");
        return instantzien_lista;
    }

    public static String removePunctuations(String source) {
        return source.replaceAll("\\p{Punct}", " ");
    }

    public static void printLista(String[] lista){
        for (int i = 0; i < lista.length; i++) {
            System.out.println("----------------------------------------------------------------------");
            System.out.println(lista[i]);
        }
        System.out.println("LISTAREN LUZEERA: " + lista.length);
    }
}