package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;

import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Resample;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;

public class arff2bow {

    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala train fitxategia</li>
     *     <li> parametro bezala bektorearen errepresentazioa: BoW (0) edo TF-IDF(1)</li>
     *     <li> parametro bezala Sparse edo NonSparse: Sparse (0) edo NonSparse(1)</li>
     *     <li> parametro bezala hiztegia gordetzeko .txt fitxategia</li>
     *     <li> parametro bezala train-aren bektore errepresentazioa gordetzeko .arff fitxategiaren izena</li>
     *     <li> parametro bezala dev gordetzeko .arff fitxategiaren izena</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 4. parametroan adierazitako .txt fitxategia</li>
     *      <li> fitxategi bezala 5. parametroan adierazitako .arff fitxategia</li>
     *      <li> fitxategi bezala 6. parametroan adierazitako .arff fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar arff2bow.jar path/to/dataRAW.arff "0/1" "0/1"  path/to/irteerako/hiztegia.txt path/to/irteerako/trainBOW.arff path/to/irteerako/devRAW.arff
     */
    public static void main(String[] args){
        try{
            getBowArff(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], args[4], args[5]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio-adibidea:\n" +
                    "\t\t\tjava -jar arff2bow.jar path/to/dataRAW.arff \"0/1\" \"0/1\"  path/to/irteerako/hiztegia.txt path/to/irteerako/trainBOW.arff path/to/irteerako/devRAW.arff\n\n");
        }
    }

    /**
     * Jasotako arff batetik, instantzien multzoa 'train' eta 'dev' partiketetan banatzen da. 'train' partiketarekin
     * errepresentazio bektoriala sortuko da.
     * @param cleanDataArffPath jasotako arff-aren path-a
     * @param errepresentazioBektoriala BoW edo TFIDF erabiliko den erabakitzeko erabiltzen da. 0 --> BoW, 1 --> TFIDF
     * @param sparse Sparse edo Non-Sparse erabiliko den erabakitzeko erabiltzen da. 0 --> Sparse, 1 --> Non-Sparse
     * @param hiztegiPath errepresentazio bektorialan erabiltzen den hiztegia gordetzeko path-a
     * @param trainBoWPath errepresntazio bektorialaren instantzia multzotik, 'train' banaketaren
     * @param devPath 'dev' partikaeta gordeko den path-a
     */
    public static void getBowArff(String cleanDataArffPath,int errepresentazioBektoriala,int sparse, String hiztegiPath, String trainBoWPath,String devPath) {

        try{

            System.out.println("ARFF GARBITIK ABIATUTA BoW SORTUKO DA" + "\n");

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(cleanDataArffPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);

            //IZENA ALDATU BEHAR ZAIO KLASEARI StringToWordVector EGIN AHAL IZATEKO
            data.renameAttribute(data.numAttributes()-1, "klasea");
            data.setClassIndex(data.numAttributes()-1);

            //TRAIN MULTZOA LORTU
            Resample resample = new Resample();
            resample.setRandomSeed(42);
            resample.setNoReplacement(true);
            resample.setInvertSelection(false);
            resample.setSampleSizePercent(70);
            resample.setInputFormat(data);
            Instances train=Filter.useFilter(data,resample);
            System.out.println("TRAIN INSTANTZIAK: " + train.numInstances() + "\n");

            //DEV MULTZOA LORTU
            resample.setRandomSeed(42);
            resample.setNoReplacement(true);
            resample.setInvertSelection(true);
            resample.setInputFormat(data);
            Instances dev= Filter.useFilter(data, resample);
            System.out.println("DEV INSTANTZIAK: " + dev.numInstances() + "\n");

            // TEST GORDE
            datuakGorde(devPath,dev);

            //StringToWordVector APLIKATU
            File hiztegia = new File(hiztegiPath); //HIZTEGIAREN FITXATEGIA SORTU
            Instances trainBoW= stringToWordVector(train, hiztegia,errepresentazioBektoriala); //TRAIN MULTZOAREN HIZTEGIA SORTU

            // SPARSE/NONSPARSE
            if(sparse==1){
                trainBoW = SparseToNonSparse(trainBoW);
            }

            trainBoW = reorder(trainBoW);
            //BoW ARFF-AN GORDE
            datuakGorde(trainBoWPath, trainBoW);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 'test' instantzia multzoari reorder
     * @param test reorder filtroa aplikatuko zaion parametroa
     * @return reorder aplikatutako instantzia multzoa
     * @throws Exception
     */
    private static Instances reorder(Instances test) throws Exception {
        Reorder filterR = new Reorder();
        filterR.setAttributeIndices("2-"+ test.numAttributes()+",1"); //2-atributu kop, 1.  2-atributu kop bitarteko atributuak goian jarriko dira eta 1 atributua (klasea dena) amaieran.
        filterR.setInputFormat(test);
        test = Filter.useFilter(test,filterR);
        return test;
    }

    /**
     * Instantzia multzo bat jasota, emandako path-ean gordeko da instantzia multzo hori arff formatuan
     * @param path gordeko den arff-aren direktorioari erreferentzia egiten dio
     * @param data instantzia multzoa
     * @throws Exception
     */
    private static void datuakGorde(String path, Instances data) throws Exception {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(path));
        saver.writeBatch();
    }

    /**
     * Instantzia multzo bat jasota, instantzia multzo horrek duen mezuaren errepresentazio bektoriala lortuko da,
     * erabakitako errepresentazio bektorial motaren arbera.
     * @param train transformatuko den instantzia multzoa
     * @param hiztegia lortutako errepresentazio bektorialaren hiztegia
     * @param bektorea errepresentazio bektorial mota adierazten du. 0 --> BoW (Bag of Words); 1 --> TFIDF
     * @return errepresentazio bektorialan lortutako instantzia multzoa
     * @throws Exception
     */
    private static Instances stringToWordVector(Instances train, File hiztegia, int bektorea) throws Exception {

        StringToWordVector stringToWordVector= new StringToWordVector();

        if(bektorea==1){
            stringToWordVector.setOutputWordCounts(true);
            stringToWordVector.setIDFTransform(true);
            stringToWordVector.setTFTransform(true);
        }
        else{
            stringToWordVector.setOutputWordCounts(false);
        }

        stringToWordVector.setAttributeIndices("first-last");
        stringToWordVector.setWordsToKeep(10000);                //defektuz 1000
        stringToWordVector.setPeriodicPruning(-1.0);

        stringToWordVector.setLowerCaseTokens(true); //MAYUS ETA MINUS ARTEKO BEREIZKETARIK EZ TRUE BADAGO
        stringToWordVector.setDictionaryFileToSaveTo(hiztegia); //HIZTEGIA GORDETZEKO FITXATEGIA EZARRI
        stringToWordVector.setInputFormat(train);
        Instances trainBOW= Filter.useFilter(train,stringToWordVector);
        return trainBOW;
    }

    /**
     * Sarrerako 'test' instantzia multzoa jasota, SparseToNonSparse filtroa aplikatuko zaio eta hori itzuliko da.
     * @param data SparseToNonSparse filtroa aplikatuko zaion instantzia multzoa
     * @return SparseToNonSparse filtroa aplikatuta daukan instantzia multzoa
     * @throws Exception
     */
    private static Instances SparseToNonSparse(Instances data) throws Exception{
        SparseToNonSparse filterNonSparse = new SparseToNonSparse();
        filterNonSparse.setInputFormat(data);
        Instances nonSparseData = Filter.useFilter(data,filterNonSparse);
        return nonSparseData;
    }

}