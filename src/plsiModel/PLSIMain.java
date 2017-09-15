package plsiModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class PLSIMain {
	public static void main(String[] args) {
		int nOfTopics = 20;
		int nOfDocs = 269;
		int nOfWords = 975;
		//HashSet<Integer> topics = new HashSet<Integer>();
		HashMap<Integer, HashMap<Integer, Double>> dataSet = new HashMap<Integer, HashMap<Integer, Double>>();
		double[][] groudTruthTheta = new double[nOfDocs][nOfTopics];
		System.out.println("Generate synthetic data: ");
		getSyntheticData(dataSet, nOfTopics, nOfDocs, nOfWords);
		PLSIModel plsi = new PLSIModel(dataSet, nOfWords, nOfTopics, nOfDocs);
		plsi.init();
		
	}

	public static double[][] getSyntheticData(HashMap<Integer, HashMap<Integer, Double>> dataSet, int nOfTopics, int nOfDocs, int nOfWords) {
		Random rand = new Random();
		double[] pDocs = new double[nOfDocs];
		double[][] pTopicDocs = new double[nOfTopics][nOfDocs];
		double[][] pWordTopics = new double[nOfWords][nOfTopics];
		// select a document with a random probability

		double sum = 0;
		for (int i = 0; i < nOfDocs; i++) {
			pDocs[i] = rand.nextDouble();
			sum += pDocs[i];
		}
		for (int i = 0; i < nOfDocs; i++) {
			pDocs[i] = pDocs[i] / sum;
		}

		// pick a latent class with a random probability
		for (int i = 0; i < nOfDocs; i++) {
			sum = 0;
			for (int j = 0; j < nOfTopics; j++) {
				pTopicDocs[j][i] = rand.nextDouble();
				sum += pTopicDocs[j][i];
			}
			for (int j = 0; j < nOfTopics; j++) {
				pTopicDocs[j][i] = pTopicDocs[j][i] / sum;
			}
		}
		// generate a word with a random probability
		for (int i = 0; i < nOfWords; i++) {
			for (int j = 0; j < nOfTopics; j++) {
				pWordTopics[i][j] = Math.random();
			}

		}

		for (int i = 0; i < nOfDocs; i++) {
			HashMap<Integer, Double> wordList = new HashMap<Integer, Double>();
			for (int j = 0; j < nOfWords; j++) {
				double p = pDocs[i];
				for (int z = 0; z < nOfTopics; z++) {
					p += pWordTopics[j][z]*pTopicDocs[z][i];
				}
				if (p > 0)
					wordList.put(j, p);
			}
			dataSet.put(i, wordList);
		}
		return pTopicDocs;
	}

	/*public static HashMap<String, HashMap<String, String>> getDataSet(String path) {
		HashMap<String, HashMap<String, String>> dataSet = new HashMap<String, HashMap<String, String>>();
		try {
			BufferedReader buff = new BufferedReader(new FileReader(path));
			String line = null;
			HashMap<String, String> docCountMap = new HashMap<String, String>();
			String elementsInLine[] = new String[3];
			while ((line = buff.readLine()) != null) {
				elementsInLine = line.split("\\s");
				if (!dataSet.containsKey(elementsInLine[0])) {
					docCountMap = new HashMap<String, String>();
					docCountMap.put(elementsInLine[1], elementsInLine[2]);
					dataSet.put(elementsInLine[0], docCountMap);
				} else {
					docCountMap = dataSet.get(elementsInLine[0]);
					docCountMap.put(elementsInLine[1], elementsInLine[2]);
				}
			}
			buff.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataSet;
	}

	public static HashSet<String> getTopicIds(String path) {
		HashSet<String> topicIds = new HashSet<String>();
		try {
			BufferedReader buff = new BufferedReader(new FileReader(path));
			String line = null;
			while ((line = buff.readLine()) != null) {
				topicIds.add(line);
			}

			buff.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return topicIds;
	}*/
}
