package plsiModel;

import java.util.HashMap;
import java.util.Random;

public class PLSIModel {
	private HashMap<Integer, HashMap<Integer, Integer>> dataSet;
	private int nOfTopics, nOfUniqueWords, nOfDocs;
	private double[][] pDZ;
	private double[][] pWZ;
	private double[] pZ;
	private double[][][] pZDW;

	public PLSIModel() {
		// TODO Auto-generated constructor stub
	}

	public PLSIModel(HashMap<Integer, HashMap<Integer, Integer>> inputData, int nOfWords, int nOfTopics, int nOfDocs) {
		dataSet = inputData;
		this.nOfDocs = nOfDocs;
		this.nOfTopics = nOfTopics;
		this.nOfUniqueWords = nOfWords;
		pZ = new double[nOfTopics];
		pDZ = new double[nOfTopics][];
		pWZ = new double[nOfTopics][];
		pZDW = new double[nOfTopics][nOfDocs][nOfUniqueWords];

	}

	public void trainingModel() {

		double llhood;
		double newllhood = getLikelihood();
		int loop = 0;
		do{
			loop++;
			llhood = newllhood;
			// E-step: Expectation step
			// System.out.println("+ E step:");
			for (int d = 0; d < nOfDocs; d++) {
				for (int w = 0; w < nOfUniqueWords; w++) {
					if (!dataSet.get(d).containsKey(w))
						continue;
					double sum = 0;
					for (int z = 0; z < nOfTopics; z++) {

						pZDW[z][d][w] = pZ[z] * pDZ[z][d] * pWZ[z][w];
						sum += pZDW[z][d][w];
					}
					for (int z = 0; z < nOfTopics; z++) {
						pZDW[z][d][w] /= sum;

					}
				}
			}
			pZ = new double[nOfTopics];
			pDZ = new double[nOfTopics][nOfDocs];
			pWZ = new double[nOfTopics][nOfUniqueWords];
			
			// M-step: Maximization step
			// System.out.println("+ M step:");
			for (int z = 0; z < nOfTopics; z++) {
				double sum = 0;
				double sumZ = 0;
				for (int w = 0; w < nOfUniqueWords; w++) {
					for (int d = 0; d < nOfDocs; d++) {
						if (!dataSet.get(d).containsKey(w))
							continue;

						double term = dataSet.get(d).get(w) * pZDW[z][d][w];
						// p(w|z) = sum_d(n(d, w)*p(z|d, w))
						pWZ[z][w] += term;

						// p(d|z) = sum_w(n(d, w')*p(z|d, w'))
						pDZ[z][d] += term;
						sum += term;

						// p(z) = sum(sum(n(d', w')P(z|d',w'))

						sumZ += dataSet.get(d).get(w);
					}
				}
				// update p(d|z) = sum(n(d, w')*p(z|d, w'))/sum(n(d', w')*p(z|d', w'))
				for (int d = 0; d < nOfDocs; d++) {
					pDZ[z][d] /= sum;
				}

				// update p(w|z) = sum_d(n(d, w)*p(z|d, w))/sum_d(sum_w(n(d, w')*p(z|d, w')))
				for (int w = 0; w < nOfUniqueWords; w++) {
					pWZ[z][w] /= sum;
				}
				// update p(z) = sum(sum(n(d', w')P(z|d',w'))/sum(sum(n(d', w')))
				pZ[z] = sum / sumZ;
			}
			newllhood = getLikelihood();
			//System.out.println("likelihood: "+llhood+"\t"+newllhood);
		}while(Math.abs(llhood - newllhood) >0.01);
		//System.out.println("number of loops until convergence: "+loop);

	}
	
	
	public void _trainingModel() {
		
	}
	public void init() {
		// init p(z)
		pZ = getRandomProbabilities(nOfTopics);

		// int pDZ
		for (int z = 0; z < nOfTopics; z++) {
			pDZ[z] = getRandomProbabilities(nOfDocs);
			pWZ[z] = getRandomProbabilities(nOfUniqueWords);

		}
	}

	public double[] getRandomProbabilities(int dimension) {
		Random rand = new Random();
		double[] probs = new double[dimension];
		double sum = 0;
		for (int i = 0; i < dimension; i++) {
			probs[i] = rand.nextDouble();
			sum += probs[i];
		}
		for (int i = 0; i < dimension; i++) {
			probs[i] /= sum;
		}

		return probs;
	}
	public double getLikelihood() {
		double result = 0;
		for(int d =0; d< nOfDocs; d++) {
			for(int w = 0; w<nOfUniqueWords; w++) {
				double sum = 0;
				for(int z = 0; z<nOfTopics; z++) {
					sum += pZ[z] *pDZ[z][d] * pWZ[z][w];
				}
				if(!dataSet.get(d).containsKey(w))
					continue;
				result += Math.log(sum)*dataSet.get(d).get(w);
				
			}
		}
		return result;
	}
	public void printParameters() {
		System.out.println("PZ:");
		for (int i = 0; i < nOfTopics; i++)
			System.out.println("Cluster: " + i + ": " + pZ[i]);
		System.out.println("............................................");
		System.out.println("PDZ:");
		for (int i = 0; i < nOfDocs; i++) {
			for (int j = 0; j < nOfTopics; j++) {
				System.out.print(pDZ[j][i] + "\t");
			}
			System.out.println();
		}
		for (int i = 0; i < nOfUniqueWords; i++) {
			for (int j = 0; j < nOfTopics; j++) {
				System.out.print(pWZ[j][i] + "\t");
			}
			System.out.println();
		}
	}

	public double[][] getPWZ() {
		return pWZ;
	}

	public double[] getPD() {
		double[] pD = new double[nOfDocs];
		for (int d = 0; d < nOfDocs; d++) {
			for (int z = 0; z < nOfTopics; z++) {
				pD[d] += pDZ[z][d] * pZ[z];
			}
		}
		return pD;
	}

	public double[][] getPZD() {
		double pZD[][] = new double[nOfTopics][nOfDocs];
		double[] pD = getPD();
		for (int z = 0; z < nOfTopics; z++) {
			for (int d = 0; d < nOfDocs; d++) {
				pZD[z][d] = pDZ[z][d] *pZ[z]/ pD[d];
			}
		}
		return pZD;
	}
	
	public double[][] getPDZ() {
		return pDZ;
	}
}