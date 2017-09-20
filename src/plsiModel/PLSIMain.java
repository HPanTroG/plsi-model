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
		int nOfTopics = 20;
		int nOfDocs = 269;
		int nOfWords = 975;
		// HashSet<Integer> topics = new HashSet<Integer>();
		HashMap<Integer, HashMap<Integer, Integer>> dataSet = new HashMap<Integer, HashMap<Integer, Integer>>();

		System.out.println("Generate synthetic data: ");
		double[][][] groudTruthTheta = getSyntheticData(dataSet, nOfTopics, nOfDocs, nOfWords);
		System.out.println("Training model:");
		PLSIModel plsi = new PLSIModel(dataSet, nOfWords, nOfTopics, nOfDocs);
		System.out.println("->> initialize parameters:");
		plsi.init();
		System.out.println("->> update parameters:");
		plsi.trainingModel();
	//	System.out.println("Complete!");
		
		/*double[][][] learntTheta = plsi.getLearntTheta();
		Scanner scan = new Scanner(System.in);
		for(int z = 0; z<nOfTopics; z++) {
			for(int d = 0; d<nOfDocs; d++) {
				for(int w = 0; w<nOfWords; w++) {
					System.out.println(learntTheta[z][d][w]+"\t"+groudTruthTheta[z][d][w]);
					scan.nextLine();
				}
			}
		}*/
	}

	public static double[][][] getSyntheticData(HashMap<Integer, HashMap<Integer, Integer>> dataSet,
			 int nOfTopics, int nOfDocs, int nOfWords) {

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
			double pros[] = getRandomProbabilities(nOfTopics);
			for (int z = 0; z < nOfTopics; z++) {
				pZD[z][d] = pros[z];

			}

		}
		// generate a word with a random probability
		for (int z = 0; z < nOfTopics; z++) {
			double[] pros = getRandomProbabilities(nOfWords);
			for (int w = 0; w < nOfWords; w++) {
				pWZ[w][z] = pros[w];
				//System.out.println(pWZ[w][z]);
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
					//System.out.println(sum);
				}
				wordProsMap.put(w, sum);
			}
			pWD.put(d, wordProsMap);
		}

		// sampling 
		for(int d = 0; d<nOfDocs; d++) {
			HashMap<Integer, Integer> wordCountMap = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> wordProsMap = pWD.get(d);
			
			//sort probabilities in increasing order
			List<Entry<Integer, Double>> sortedWordProsMap = new ArrayList<Entry<Integer, Double>>(wordProsMap.entrySet());
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
			for(int i = 1; i<nOfWords; i++) {
				cdf[i]=cdf[i-1]+sortedWordProsMap.get(i).getValue();
			}
			
			int nOfWordInDocument = rand.nextInt(nOfWords-1)+1; 
			for(int i = 0; i<nOfWordInDocument; i++) {
				 int r = Math.abs(Arrays.binarySearch(cdf, rand.nextDouble()/100));
				 if(r== sortedWordProsMap.size()) r--;
				 int word = sortedWordProsMap.get(r).getKey();
				 if(wordCountMap.containsKey(word)) {
					 int count = wordCountMap.get(word);
					 wordCountMap.put(word, count+1);
				 } else
					 wordCountMap.put(word, 1);
			}
			
		
			dataSet.put(d, wordCountMap);
		}
		for(Map.Entry<Integer, HashMap<Integer, Integer>> docx: dataSet.entrySet()) {
			HashMap<Integer, Integer> words = docx.getValue();
			
			for(Map.Entry<Integer, Integer> wordCounts: words.entrySet()) {
				System.out.println("d: "+docx.getKey()+"\tw: " + wordCounts.getKey()+"\t"+ wordCounts.getValue());
			}
		}
		
		// get groundTruth
		
		for(int d = 0; d<nOfDocs; d++) {
			for(int w = 0; w<nOfWords; w++) {
				if(!dataSet.get(d).containsKey(w))
					continue;
				sum = 0;
				for(int z = 0; z<nOfTopics; z++) {
					pZDW[z][d][w] = pZD[z][d] * pWZ[w][z];
					sum += pZDW[z][d][w];
				}
				for(int z = 0; z<nOfTopics; z++) {
					pZDW[z][d][w] /= sum;
					
				}
			}
		}
		
		
		return pZDW;
	}

	/*public static HashMap<Integer, HashMap<Integer, Integer>> getSampleData(double[][] pWD) {
		HashMap<Integer, HashMap<Integer, Integer>> dataSet = new HashMap<Integer, HashMap<Integer, Integer>>();
		for (int d = 0; d < pWD[0].length; d++) {
			
		}
		return dataSet;
	}*/

	// get a normalize array
	public static double[] getRandomProbabilities(int length) {
		double[] pros = new double[length];
		double sum = 0;
		Random rand = new Random();
		for (int i = 0; i < length; i++) {
			pros[i] = rand.nextDouble();
			sum += pros[i];
		}
		for (int i = 0; i < length; i++) {
			pros[i] = pros[i] / sum;
		}

		return pros;
	}


}
