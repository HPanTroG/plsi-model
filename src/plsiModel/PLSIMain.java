package plsiModel;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PLSIMain {
	public static void main(String[] args) {

		int nOfTopics = 10;
		int nOfDocs = 10;
		int nOfWords = 1000;
		double[] pDocs = new double[nOfDocs];
		double[][] pZD = new double[nOfDocs][];
		double[][] pWZ = new double[nOfTopics][];
		// HashSet<Integer> topics = new HashSet<Integer>();
		HashMap<Integer, HashMap<Integer, Integer>> dataSet = new HashMap<Integer, HashMap<Integer, Integer>>();

		//System.out.println("Generate synthetic data: ");
		getSyntheticData(dataSet, pDocs, pZD, pWZ, nOfTopics, nOfDocs, nOfWords, 10000);
		//printDataset(dataSet);

		
		PLSIModel plsi = new PLSIModel(dataSet, nOfWords, nOfTopics, nOfDocs);
		plsi.init();

		plsi.trainingModel();
		
		double[] learntPD = plsi.getPD();

		
		double[][] learntPZD = plsi.getPZD();
		
	
		double[][] learntPWZ = plsi.getPWZ();
		

		
		System.out.println("KL divergence between pDs: " + evaluate(learntPD, pDocs));
		System.out.println("KL divergence between pZDs: "+ evaluate(getOneDimenArrFromTwoDimensionArr(learntPZD), getOneDimenArrFromTwoDimensionArr(pZD)));
		System.out.println("KL divergence between pWZs: "+ evaluate(getOneDimenArrFromTwoDimensionArr(learntPWZ), getOneDimenArrFromTwoDimensionArr(pWZ)));
		
	}
	public static double[] getOneDimenArrFromTwoDimensionArr(double[][] matrix) {
		double[] array = new double[matrix.length * matrix[0].length];
		int k = 0;
		for(int i = 0; i<matrix.length; i++) {
			for(int j = 0; j<matrix[0].length; j++) {
				array[k++] = matrix[i][j];
			}
		}
		
		return array;
	}
	
	public static double  evaluate(double[][] learntArray, double[][] groundTruth) {
		double result = 0;
		for(int i = 0; i<learntArray.length; i++) {
			for(int j = 0; j<learntArray[0].length; j++) {
				result += groundTruth[i][j] * Math.log(groundTruth[i][j]/learntArray[i][j]);
			}
		}
		return result;
	}
	public static double evaluate(double[] learntParameters, double[] groundTruth) {
		double klMeasure = 0;
		for(int i = 0; i< groundTruth.length; i++) {
			klMeasure += groundTruth[i]*Math.log(groundTruth[i]/learntParameters[i]);
		}
		return klMeasure;
	}

	/***
	 * generate a random multinomial distribution
	 * 
	 * @param dimension
	 * @return
	 */
	public static double[] genRandomMultinomialDistribution(int dimension) {
		double[] pros = new double[dimension];
		double sum = 0;
		Random rand = new Random();
		for (int i = 0; i < dimension; i++) {
			pros[i] = rand.nextDouble();
			sum += pros[i];
		}
		for (int i = 0; i < dimension; i++) {
			pros[i] = pros[i] / sum;
		}

		return pros;
	}

	/***
	 * generate a skew random multinomial distribution
	 * 
	 * @param dimension
	 * @param k:
	 *            k% of dimension to skew on
	 * @param p:
	 *            skewness of top k% of dimenion i.e., sum of probs of top k% of
	 *            dimenion is at least p%
	 * @return
	 */
	public static double[] genRandomSkewMultinomialDistribution(int dimension, int k, int p) {
		int n = k * dimension / 100; // get #dimensions to skew on
		int m = dimension - n;
		Random rand = new Random();
		double[] skewed = genRandomMultinomialDistribution(n);
		double[] nonSkewed = genRandomMultinomialDistribution(m);

		double[] probs = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			probs[i] = -1;
		}
		for (int i = 0; i < n; i++) {
			int j = rand.nextInt(dimension);
			while (probs[j] != -1) {
				j = rand.nextInt(dimension);
			}
			probs[j] = skewed[i] * ((double) p / 100);
		}

		int j = 0;
		for (int i = 0; i < m; i++) {
			while (probs[j] != -1) {
				j++;
			}
			probs[j] = nonSkewed[i] * (1 - (double) p / 100);
		}

		return probs;
	}

	public static int sampleMultinomial(double[] prob, Random rand) {
		double x = rand.nextDouble();
		double sum = 0;
		for (int i = 0; i < prob.length; i++) {
			sum += prob[i];
			if (sum >= x) {
				return i;
			}
		}
		return prob.length - 1;
	}

	/***
	 * 
	 * @param dataSet
	 * @param nOfTopics
	 * @param nOfDocs
	 * @param nOfWords
	 * @param size:
	 *            #words to be generated
	 */
	public static void getSyntheticData(HashMap<Integer, HashMap<Integer, Integer>> dataSet, double[] pDocs,
			double[][] pZD, double[][] pWZ, int nOfTopics, int nOfDocs, int nOfWords, int size) {

		// select a document with a random probability
		for (int d = 0; d < nOfDocs; d++) {
			pDocs[d] = (double)1 / nOfDocs;
		
		}

		// pick a latent class with a random probability
		for (int d = 0; d < nOfDocs; d++) {
			pZD[d] = genRandomSkewMultinomialDistribution(nOfTopics, 10, 90);
		}

		// generate a word with a random probability
		for (int z = 0; z < nOfTopics; z++) {
			pWZ[z] = genRandomSkewMultinomialDistribution(nOfWords, 1, 90);

		}
		Random rand = new Random();
		for (int i = 0; i < size; i++) {

			// int d = sampleMultinomial(pDocs);
			int d = rand.nextInt(nOfDocs);

			int z = sampleMultinomial(pZD[d], rand);
			int w = sampleMultinomial(pWZ[z], rand);

			HashMap<Integer, Integer> wordCount = dataSet.get(d);
			if (wordCount == null) {
				wordCount = new HashMap<>();
				wordCount.put(w, 1);
				dataSet.put(d, wordCount);
			} else {
				if (wordCount.containsKey(w)) {
					wordCount.put(w, 1 + wordCount.get(w));
				} else {
					wordCount.put(w, 1);
				}
			}
		}

	}

	public static void printDataset(HashMap<Integer, HashMap<Integer, Integer>> dataSet) {
		for (Map.Entry<Integer, HashMap<Integer, Integer>> doc : dataSet.entrySet()) {
			int d = doc.getKey();
			for (Map.Entry<Integer, Integer> uniqueWord : doc.getValue().entrySet()) {
				int w = uniqueWord.getKey();
				int count = uniqueWord.getValue();
				System.out.printf("d = %d w = %d count = %d\n", d, w, count);
			}
		}

	}

}
