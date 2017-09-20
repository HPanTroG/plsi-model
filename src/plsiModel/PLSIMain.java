package plsiModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class PLSIMain {
	public static void main(String[] args) {

		/*
		 * double[] p = genRandomSkewMultinomialDistribution(10, 10, 90);
		 * 
		 * for (int i = 0; i < p.length; i++) { System.out.printf("p[%d] = %f\n"
		 * , i, p[i]); }
		 * 
		 * System.exit(-1);
		 */
		int nOfTopics = 10;
		int nOfDocs = 10;
		int nOfWords = 1000;
		// HashSet<Integer> topics = new HashSet<Integer>();
		HashMap<Integer, HashMap<Integer, Integer>> dataSet = new HashMap<Integer, HashMap<Integer, Integer>>();

		System.out.println("Generate synthetic data: ");
		// double[][][] groudTruthTheta = getSyntheticData(dataSet, nOfTopics,
		// nOfDocs, nOfWords);
		getSyntheticData_TA(dataSet, nOfTopics, nOfDocs, nOfWords, 10000);
		printDataset(dataSet);

		System.out.println("Training model:");
		PLSIModel plsi = new PLSIModel(dataSet, nOfWords, nOfTopics, nOfDocs);
		System.out.println("->> initialize parameters:");
		plsi.init();
		System.out.println("->> update parameters:");
		plsi.trainingModel();
		// System.out.println("Complete!");

		/*
		 * double[][][] learntTheta = plsi.getLearntTheta(); Scanner scan = new
		 * Scanner(System.in); for(int z = 0; z<nOfTopics; z++) { for(int d = 0;
		 * d<nOfDocs; d++) { for(int w = 0; w<nOfWords; w++) {
		 * System.out.println(learntTheta[z][d][w]+"\t"+groudTruthTheta[z][d][w]
		 * ); scan.nextLine(); } } }
		 */
	}

	public static double[][][] getSyntheticData(HashMap<Integer, HashMap<Integer, Integer>> dataSet, int nOfTopics,
			int nOfDocs, int nOfWords) {

		double[] pDocs = new double[nOfDocs];
		double[][] pZD = new double[nOfTopics][nOfDocs];
		double[][] pWZ = new double[nOfWords][nOfTopics];
		double[][][] pZDW = new double[nOfTopics][nOfDocs][nOfWords];
		// select a document with a random probability

		double sum = 0;
		for (int d = 0; d < nOfDocs; d++) {
			pDocs[d] = 1 / nOfDocs;
		}

		// pick a latent class with a random probability
		for (int d = 0; d < nOfDocs; d++) {
			double pros[] = genRandomMultinomialDistribution(nOfTopics);
			for (int z = 0; z < nOfTopics; z++) {
				pZD[z][d] = pros[z];
			}
		}

		// generate a word with a random probability
		for (int z = 0; z < nOfTopics; z++) {
			double[] pros = genRandomMultinomialDistribution(nOfWords);
			for (int w = 0; w < nOfWords; w++) {
				pWZ[w][z] = pros[w];
				// System.out.println(pWZ[w][z]);
			}

		}

		// sampling from a multinomial distribution
		// get P(w|d)
		HashMap<Integer, HashMap<Integer, Double>> pWD = new HashMap<Integer, HashMap<Integer, Double>>();
		for (int d = 0; d < nOfDocs; d++) {
			HashMap<Integer, Double> wordProsMap = new HashMap<Integer, Double>();
			for (int w = 0; w < nOfWords; w++) {
				sum = 0;
				for (int z = 0; z < nOfTopics; z++) {
					sum += pWZ[w][z] * pZD[z][d];
					// System.out.println(sum);
				}
				wordProsMap.put(w, sum);
			}
			pWD.put(d, wordProsMap);
		}

		// p(w,d)

		// sampling
		for (int d = 0; d < nOfDocs; d++) {
			HashMap<Integer, Integer> wordCountMap = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> wordProsMap = pWD.get(d);

			// sort probabilities in increasing order
			List<Entry<Integer, Double>> sortedWordProsMap = new ArrayList<Entry<Integer, Double>>(
					wordProsMap.entrySet());
			Collections.sort(sortedWordProsMap, new Comparator<Map.Entry<Integer, Double>>() {

				@Override
				public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
					// TODO Auto-generated method stub
					return o1.getValue().compareTo(o2.getValue());
				}

			});

			Random rand = new Random();
			double[] cdf = new double[nOfWords];
			cdf[0] = sortedWordProsMap.get(0).getValue();
			for (int i = 1; i < nOfWords; i++) {
				cdf[i] = cdf[i - 1] + sortedWordProsMap.get(i).getValue();
			}
			// cdf[nOfWords-1] = 1

			int docLength = rand.nextInt(nOfWords - 1) + 1;
			// is this doc's length?
			// or #unique words in doc

			for (int i = 0; i < docLength; i++) {
				int r = Math.abs(Arrays.binarySearch(cdf, rand.nextDouble() / 100));
				if (r == sortedWordProsMap.size())
					r--;
				int word = sortedWordProsMap.get(r).getKey();
				if (wordCountMap.containsKey(word)) {
					int count = wordCountMap.get(word);
					wordCountMap.put(word, count + 1);
				} else
					wordCountMap.put(word, 1);
			}
			dataSet.put(d, wordCountMap);
		}
		for (Map.Entry<Integer, HashMap<Integer, Integer>> docx : dataSet.entrySet()) {
			HashMap<Integer, Integer> words = docx.getValue();

			for (Map.Entry<Integer, Integer> wordCounts : words.entrySet()) {
				System.out
						.println("d: " + docx.getKey() + "\tw: " + wordCounts.getKey() + "\t" + wordCounts.getValue());
			}
		}

		// get groundTruth

		for (int d = 0; d < nOfDocs; d++) {
			for (int w = 0; w < nOfWords; w++) {
				if (!dataSet.get(d).containsKey(w))
					continue;
				sum = 0;
				for (int z = 0; z < nOfTopics; z++) {
					pZDW[z][d][w] = pZD[z][d] * pWZ[w][z];
					sum += pZDW[z][d][w];
				}
				for (int z = 0; z < nOfTopics; z++) {
					pZDW[z][d][w] /= sum;

				}
			}
		}

		return pZDW;
	}

	/*
	 * public static HashMap<Integer, HashMap<Integer, Integer>>
	 * getSampleData(double[][] pWD) { HashMap<Integer, HashMap<Integer,
	 * Integer>> dataSet = new HashMap<Integer, HashMap<Integer, Integer>>();
	 * for (int d = 0; d < pWD[0].length; d++) {
	 * 
	 * } return dataSet; }
	 */

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
	public static void getSyntheticData_TA(HashMap<Integer, HashMap<Integer, Integer>> dataSet, int nOfTopics,
			int nOfDocs, int nOfWords, int size) {

		double[] pDocs = new double[nOfDocs];
		double[][] pZD = new double[nOfDocs][];
		double[][] pWZ = new double[nOfTopics][];

		// select a document with a random probability
		for (int d = 0; d < nOfDocs; d++) {
			pDocs[d] = 1 / nOfDocs;
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
