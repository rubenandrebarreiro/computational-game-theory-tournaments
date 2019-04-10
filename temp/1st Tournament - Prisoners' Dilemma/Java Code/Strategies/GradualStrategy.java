package play;

/**
*
* ~~~ Prisoners' Dilemma ~~~
*
* Theory of Computational Games
* 
* Practical Lab Work Assignment/Project #1 (for the 1st Tournament).
* 
* Integrated Master of Computer Science and Engineering
* Faculty of Science and Technology of New University of Lisbon
* 
* Authors:
* @author Ruben Andre Barreiro - r.barreiro@campus.fct.unl.pt
*
*/

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gametree.GameNode;
import gametree.GameNodeDoesNotExistException;
import play.exception.InvalidStrategyException;

/**
 * Class responsible for the Gradual Strategy, extending Strategy.
 * 
 * Description:
 * - A class responsible for an Implementation of the Prisoners' Dilemma.
 */
public class GradualStrategy extends Strategy {

	// Invariants/Constraints:
	
	/**
	 * The available "Cooperate" action
	 */
	private static final String COOPERATE = "Cooperate";
	
	/**
	 * The available "Defect" action
	 */
	private static final String DEFECT = "Defect";
	
	/**
	 * The number of consecutive "Cooperate" actions,
	 * during a "Calm Down" process
	 */
	private static final int NUM_COOPERATES_CALM_DOWN = 2;
	
	
	// Global Instance Variables:
	
	/**
	 * The numbers of Defects of the both, Players' Opponents
	 */
	private int[] currentNumOpponentDefects = {0, 0};
	
	/**
	 * The numbers of Defects remaining, in a Defecting process,
	 * for the both Players
	 */
	private int[] numDefectsRemaining = {0, 0};
	
	/**
	 * The numbers of Cooperates remaining, in a "Calm Down" process,
	 * for the both Players
	 */
	private int[] numCooperatesRemaining = {0, 0};
	
	/**
	 * The boolean values, too keep the information about if,
	 * there's some Punishments currently pending or not,
	 * for the both Players
	 */
	private boolean[] pendingPunishments = {false, false};
	
	
	// Methods/Functions:
	
	/**
	 * Returns true if, the Player related to a given number,
	 * it's currently Defecting and false, otherwise
	 * 
	 * @param numPlayer the number of the Player,
	 *        that it's pretending to be verified
	 *        if it's currently Defecting or not
	 * 
	 * @return true if, the Player related to a given number,
	 *         it's currently Defecting and false, otherwise
	 */
	private boolean currentlyDefecting(int numPlayer) {
		return (numDefectsRemaining[ (numPlayer - 1) ] > 0);
	}
	
	/**
	 * Returns true if, the Player related to a given number,
	 * it's currently "Calming Down" and false, otherwise.
	 * 
	 * @param numPlayer the number of the Player,
	 *        that it's pretending to be verified
	 *        if it's currently "Calming Down" or not
	 * 
	 * @return true if, the Player related to a given number,
	 *         it's currently "Calming Down" and false, otherwise
	 */
	private boolean currentlyCalmingDown(int numPlayer) {
		return (numCooperatesRemaining[ (numPlayer - 1) ] > 0);
	}

	/**
	 * Returns true if, the Player related to a given number,
	 * it's currently Defecting or "Calming Down" and false, otherwise.
	 * 
	 * @param numPlayer the number of the Player,
	 *        that it's pretending to be verified
	 *        if it's currently Defecting or "Calming Down", or not
	 * 
	 * @return true if, the Player related to a given number,
	 *         it's currently Defecting or "Calming Down" and false, otherwise
	 */
	private boolean currentlyDefectingOrCalmingDown(int numPlayer) {
		return ( this.currentlyDefecting(numPlayer) || this.currentlyCalmingDown(numPlayer) );
	}
	
	/**
	 * Starts a set of punishments, by doing, a given number of Defects and then,
	 * "Calm Down" (2 Consecutive Cooperates).
	 * 
	 * @param numPlayer the number of the Player, that it's pretended
	 * 		  to be started a set of punishments  
	 * 
	 * @param numDefects the number of Defects to be
	 *        associated to this set of punishments
	 */
	private void startDefectAndCalmDownAsPunishment(int numPlayer, int numDefects) {
		this.numDefectsRemaining[ (numPlayer - 1) ] = numDefects;
		this.numCooperatesRemaining[ (numPlayer - 1) ] = NUM_COOPERATES_CALM_DOWN;
	}
		
	/**
	 * Performs a punishment, associated to a given Player.
	 * 
	 * @param numPlayer the number of the Player,
	 *        to who be applied this punishment
	 */
	private void defectAndCalmDownAsPunishment(int numPlayer) {
		
		// I still have some previous consecutive Defects to do
		if(numDefectsRemaining[ (numPlayer - 1) ] > 0) {
				
			// I will Defect
			numDefectsRemaining[ (numPlayer - 1) ]--;
		}
			
		// I'm not currently consecutively Defecting,
		// but probably, I'm currently "Calming Down"
		// (2 consecutive Cooperates)
		else if((numDefectsRemaining[( numPlayer - 1) ] == 0) &&
				(numCooperatesRemaining[ (numPlayer - 1) ] > 0)) {
				
			// I'm "Calming Down", so, I will Cooperate
			numCooperatesRemaining[ (numPlayer - 1) ]--;
		}
	}
	
	/**
	 * The method to perform a possible Cooperate action, knowing that my Opponent Cooperate in the previous round.
	 * 
	 * @param myStrategy the Strategy's object, that's currently being used
	 * 
	 * @param numPlayer the number of the Player's Opponent, that's being analysed the possible move
	 * 
	 * @param possibleMove the possible Move, that's being analysed
	 */
	private void possibleCooperateActionKnowingThatMyOpponentCooperateInPreviousRound(PlayStrategy myStrategy,
			                                                                          int numPlayer, String possibleMove) {
		
		// I'm deciding if I Cooperate,
		// knowing that my Opponent Cooperate in the last round
		
		// But, I'm not currently consecutively Defecting neither
		// currently "Calming Down" (2 consecutive Cooperates)
		if(!this.currentlyDefectingOrCalmingDown(numPlayer)) {

			// I have some pending Punishments,
			// so, I will Defect, C = 0.0, accordingly to [C = 0.0; D = 1.0]
			if(pendingPunishments[ (numPlayer - 1) ]) {
				this.startDefectAndCalmDownAsPunishment(numPlayer, this.currentNumOpponentDefects[ (numPlayer - 1) ]);
				pendingPunishments[ (numPlayer - 1) ] = false;
			
				System.out.println("I'm not currently consecutively Defecting neither currently \"Calming Down\", but I have some pending Punishments!!!");
				
				// I'm Defecting,
				// so, I will Defect, C = 0.0, accordingly to [C = 0.0; D = 1.0]
				myStrategy.put(possibleMove, new Double(0.0));
				System.out.println("Setting " + possibleMove + " with probability of 0.0");
			}
			
			// I'm not currently consecutively Defecting neither
			// currently "Calming Down" (2 consecutive Cooperates),
			
			// So, I will do the same of my opponent in the previous round
			// by mimic (Cooperate)
			else {
				System.out.println("I'm not currently consecutively Defecting neither currently \"Calming Down\", so I will mimic and Cooperate!!!");
				
				// I'm Cooperating,
				// so, I will Cooperate, C = 1.0, accordingly to [C = 1.0; D = 0.0]
				myStrategy.put(possibleMove, new Double(1.0));
				System.out.println("Setting " + possibleMove + " with probability of 1.0");
			}
		}
		
		// Possibly, currently consecutively Defecting or
		// currently "Calming Down" (2 consecutive Cooperates) 
		else {
			
			// I still have some previous consecutive Defects to do
			if(this.currentlyDefecting(numPlayer)) {
				
				// I'm Defecting,
				// so, I will Defect, C = 0.0, accordingly to [C = 0.0; D = 1.0]
				myStrategy.put(possibleMove, new Double(0.0));
				System.out.println("Setting " + possibleMove + " with probability of 0.0");
			}
			
			// I'm not currently consecutively Defecting,
			// but probably, I'm currently "Calming Down"
			// (2 consecutive Cooperates)
			else if(this.currentlyCalmingDown(numPlayer)) {
				
				// I'm "Calming Down",
				// so, I will Cooperate, C = 1.0, accordingly to [C = 1.0; D = 0.0]
				myStrategy.put(possibleMove, new Double(1.0));
				System.out.println("Setting " + possibleMove + " with probability of 1.0");
			}
		}
	}

	/**
	 * The method to perform a possible Cooperate action, knowing that my Opponent Defects in the previous round.
	 * 
	 * @param myStrategy the Strategy's object, that's currently being used
	 * 
	 * @param numPlayer the number of the Player's Opponent, that's being analysed the possible move
	 * 
	 * @param possibleMove the possible Move, that's being analysed
	 */
	private void possibleCooperateActionKnowingThatMyOpponentDefectInPreviousRound(PlayStrategy myStrategy, int numPlayer, String possibleMove) {
		
		// I'm deciding if I Cooperate,
		// knowing that my Opponent Defect in the last round
		
		// But, I'm not currently consecutively Defecting neither
		// currently "Calming Down" (2 consecutive Cooperates)
		if(!this.currentlyDefectingOrCalmingDown(numPlayer)) {

			// I will make so many Defects as my Opponent,
			// and after that, I will "Calm Down"
			// (2 consecutive Cooperates)
		
			// I'm Defecting,
			// so, I will Defect, C = 0.0, accordingly to [C = 0.0; D = 1.0]
			myStrategy.put(possibleMove, new Double(0.0));
			System.out.println("Setting " + possibleMove + " with probability of 0.0");
		}
		
		// Possibly, currently consecutively Defecting or
		// currently "Calming Down" (2 consecutive Cooperates) 
		else {
			
			// I still have some previous consecutive Defects to do
			if(this.currentlyDefecting(numPlayer)) {
				
				// I'm Defecting,
				// so, I will Defect, C = 0.0, accordingly to [C = 0.0; D = 1.0]
				myStrategy.put(possibleMove, new Double(0.0));
				System.out.println("Setting " + possibleMove + " with probability of 0.0");
			}
			
			// I'm not currently consecutively Defecting,
			// but probably, I'm currently "Calming Down"
			// (2 consecutive Cooperates)
			else if(this.currentlyCalmingDown(numPlayer)) {
				
				// I'm "Calming Down",
				// so, I will Cooperate, C = 1.0, accordingly to [C = 1.0; D = 0.0]
				myStrategy.put(possibleMove, new Double(1.0));
				System.out.println("Setting " + possibleMove + " with probability of 1.0");
			}
		}
	}

	/**
	 * The method to perform a possible Defect action, knowing that my Opponent Cooperate in the previous round.
	 * 
	 * @param myStrategy the Strategy's object, that's currently being used
	 * 
	 * @param numPlayer the number of the Player's Opponent, that's being analysed the possible move
	 * 
	 * @param possibleMove the possible Move, that's being analysed
	 */
	private void possibleDefectActionKnowingThatMyOpponentCooperateInPreviousRound(PlayStrategy myStrategy, int numPlayer, String possibleMove) {
		
		// I'm deciding if I Defect,
		// knowing that my Opponent Cooperate in the last round
		
		// But, I'm not currently consecutively Defecting neither
		// currently "Calming Down" (2 consecutive Cooperates)
		if(!this.currentlyDefectingOrCalmingDown(numPlayer)) {
			
			// I have some pending Punishments 
			if(pendingPunishments[ (numPlayer - 1) ]) {
				this.startDefectAndCalmDownAsPunishment(numPlayer, this.currentNumOpponentDefects[ (numPlayer - 1) ]);
				pendingPunishments[ (numPlayer - 1) ] = false;
				
				// So, I will Defect, C = 0.0, accordingly to [C = 0.0; D = 1.0]
				myStrategy.put(possibleMove, new Double(1.0));
				System.out.println("Setting " + possibleMove + " with probability of 1.0");
	
				// Attempts to make a Defect and Calm Down punishment,
				// accordingly to the Gradual strategy
				this.defectAndCalmDownAsPunishment(numPlayer);
			}
			
			// So, I will do the same of my opponent in the previous round
			// by mimic (Cooperate)
			else {
				
				// So, I will Cooperate, D = 0.0, accordingly to [C = 1.0; D = 0.0]
				myStrategy.put(possibleMove, new Double(0.0));
				System.out.println("Setting " + possibleMove + " with probability of 0.0");
			}
		}
		
		// Possibly, currently consecutively Defecting or
		// currently "Calming Down" (2 consecutive Cooperates) 
		else {
			
			// I still have some previous consecutive Defects to do
			if(this.currentlyDefecting(numPlayer)) {
				
				// I'm Defecting,
				// so, I will Defect, D = 1.0, accordingly to [C = 0.0; D = 1.0]
				myStrategy.put(possibleMove, new Double(1.0));
				System.out.println("Setting " + possibleMove + " with probability of 1.0");
			}
			
			// I'm not currently consecutively Defecting,
			// but probably, I'm currently "Calming Down"
			// (2 consecutive Cooperates)
			else if(this.currentlyCalmingDown(numPlayer)) {
				
				// I'm "Calming Down",
				// so, I will Cooperate, D = 0.0, accordingly to [C = 1.0; D = 0.0]
				myStrategy.put(possibleMove, new Double(0.0));
				System.out.println("Setting " + possibleMove + " with probability of 0.0");
			}
		
			// Attempts to make a Defect and Calm Down punishment,
			// accordingly to the Gradual strategy
			this.defectAndCalmDownAsPunishment(numPlayer);
		}
	}

	private void possibleDefectActionKnowingThatMyOpponentDefectInPreviousRound(PlayStrategy myStrategy, int numPlayer, String possibleMove) {
		
		// I'm deciding if I Defect,
		// knowing that my Opponent Defect in the last round
				
		// I detect a Defect action made by my Opponent in the last round
		this.currentNumOpponentDefects[numPlayer - 1]++;
		
		// I will pass to have a new pending Punishments
		this.pendingPunishments[numPlayer - 1] = true;
		
		// But, I'm not currently consecutively Defecting neither
		// currently "Calming Down" (2 consecutive Cooperates)
		
		// So, I will Defect and continue to do it, so,
		// until I done so many Defects as my Opponent at the moment,
		// and after, I will "Calm Down" (2 consecutive Cooperates)
		if(!this.currentlyDefectingOrCalmingDown(numPlayer)) {

			// I will make so many Defects as my Opponent,
			// and after that, I will "Calm Down"
			// (2 consecutive Cooperates)
			this.startDefectAndCalmDownAsPunishment(numPlayer, this.currentNumOpponentDefects[numPlayer - 1]);
			
			// I'm Defecting,
			// so, I will Defect, D = 1.0, accordingly to [C = 0.0; D = 1.0]
			myStrategy.put(possibleMove, new Double(1.0));
			System.out.println("Setting " + possibleMove + " with probability of 1.0");
		}
		
		// Possibly, currently consecutively Defecting or
		// currently "Calming Down" (2 consecutive Cooperates) 
		else {
			
			// I still have some previous consecutive Defects to do
			if(this.currentlyDefecting(numPlayer)) {
				
				// I'm Defecting,
				// so, I will Defect, D = 1.0, accordingly to [C = 0.0; D = 1.0]
				myStrategy.put(possibleMove, new Double(1.0));
				
				System.out.println("Setting " + possibleMove + " with probability of 1.0");
			}
			
			// I'm not currently consecutively Defecting,
			// but probably, I'm currently "Calming Down"
			// (2 consecutive Cooperates)
			else if(this.currentlyCalmingDown(numPlayer)) {
				
				// I'm "Calming Down",
				// so, I will Cooperate, D = 0.0, accordingly to [C = 1.0; D = 0.0]
				myStrategy.put(possibleMove, new Double(0.0));
				System.out.println("Setting " + possibleMove + " with probability of 0.0");
			}
		}
		
		// Attempts to make a Defect and Calm Down punishment,
		// accordingly to the Gradual strategy
		this.defectAndCalmDownAsPunishment(numPlayer);
	}
	
	/**
	 * Returns the reverse path, by backward, from a given current Game Node.
	 * 
	 * @param current the current Game Node,
	 *        from it's being calculated the reverse path, by backward
	 * 
	 * @return the reverse path, by backward, from a given current Game Node
	 */
	private List<GameNode> getReversePath(GameNode current) {		
		try {
			GameNode n = current.getAncestor();
			List<GameNode> l =  getReversePath(n);
			l.add(current);
			return l;
		} catch (GameNodeDoesNotExistException e) {
			List<GameNode> l = new ArrayList<GameNode>();
			l.add(current);
			return l;
		}
	}
	
	/**
	 * Computes the Game Strategy, that I defined previously. It's here where will be applied all the computation for my strategy.
	 * 
	 * @param listP1 the list of Game Nodes of my Game Tree, as Player no. 1
	 * 
	 * @param listP2 the list of Game Nodes of my Game Tree, as Player no. 2
	 * 
	 * @param myStrategy the computational strategy, that I defined previously and that will be used by me for the current Game
	 * 
	 * @param random a Secure Random object, to calculate random numbers' operations
	 * 
	 * @throws GameNodeDoesNotExistException a GameNodeDoesNotExist to be thrown if
	 *         the a certain Game Node don't exist in the current Game
	 */
	private void cumputeStrategy(List<GameNode> listP1, List<GameNode> listP2,
														PlayStrategy myStrategy, SecureRandom random) 
														  						 throws GameNodeDoesNotExistException {
	
		Set<String> opponentMoves = new HashSet<String>();
		
		// When I played as Player no. 1, I'm going to check
		// what were the moves of my opponent as Player no. 2
		for(GameNode n: listP1) {
			if(n.isNature() || n.isRoot()) continue;
			
			if(n.getAncestor().isPlayer2()) {
				opponentMoves.add(n.getLabel());
			}
		}
		
		// When I played as Player no. 2, I'm going to check
		// what were the moves of my opponent as Player no. 1
		for(GameNode n: listP2) {
			if(n.isNature() || n.isRoot()) continue;
			
			if(n.getAncestor().isPlayer1()) {
				opponentMoves.add(n.getLabel());
			}
		}

		System.out.println();
		
		System.out.println("My Opponent's Plays:");
		for(String opponentMove : opponentMoves) {
			System.out.println("- " + opponentMove);
		}
		
		System.out.println();
		
		Iterator<String> moves = myStrategy.keyIterator();
		
		// I will analyse all the possible moves
		while(moves.hasNext()) {
			
			// The current possible move
			String currentMove = moves.next();
			
			System.out.println();
			System.out.println();
			
			System.err.println("Analysing " + currentMove + " ...");
			
			System.err.println();
			
			String[] playStructure = currentMove.split(":");
			
			int currentOpponentPlayer = Integer.parseInt(playStructure[0]);
			String currentAction = playStructure[2];
			
			// Currently, analysing a possible Cooperate action,
			// before I decide
			if(currentAction.equalsIgnoreCase(COOPERATE)) {
				
				// In this case, my opponent Cooperates in the previous round
				if(opponentMoves.contains(currentMove)) {
					System.err.println("My Opponent (as Player no. " + currentOpponentPlayer + ") Cooperates in the last round!!!");
					this.possibleCooperateActionKnowingThatMyOpponentCooperateInPreviousRound(myStrategy, currentOpponentPlayer, currentMove);
				}
				
				// In this case, my opponent Defect in the previous round
				else {
					System.err.println("My Opponent (as Player no. " + currentOpponentPlayer + ") Defects in the last round!!!");
					this.possibleCooperateActionKnowingThatMyOpponentDefectInPreviousRound(myStrategy, currentOpponentPlayer, currentMove);
				}
			}
			
			// Currently, analysing a possible Defect action,
			// before I decide
			if(currentAction.equalsIgnoreCase(DEFECT)) {
					
				// In this case, my opponent Defect in the previous round
				if(opponentMoves.contains(currentMove)) {
					System.err.println("My Opponent (as Player no. " + currentOpponentPlayer + ") Defects in the last round!!!");
					this.possibleDefectActionKnowingThatMyOpponentDefectInPreviousRound(myStrategy, currentOpponentPlayer, currentMove);
				}
				
				// In this case, my opponent Cooperates in the previous round
				else {
					System.err.println("My Opponent (as Player no. " + currentOpponentPlayer + ") Cooperates in the last round!!!");
					this.possibleDefectActionKnowingThatMyOpponentCooperateInPreviousRound(myStrategy, currentOpponentPlayer, currentMove);
				}
			}
		}	
		
		System.out.println();
		System.out.println();
		
		// Print the current number of Defect of the Opponents
		System.out.println("Number of Defects of the Opponent of Player no. 1: " + this.currentNumOpponentDefects[0]);
		System.out.println("Number of Defects of the Opponent of Player no. 2: " + this.currentNumOpponentDefects[1]);
		
		System.out.println();
		
		// The following piece of code has the goal of checking if there was a portion
		// of the game for which we could not infer the moves of the adversary
		// (because none of the current Game's plays in the previous round pass through those paths)
		Iterator<Integer> validationSetIte = tree.getValidationSet().iterator();
		moves = myStrategy.keyIterator();
		
		while(validationSetIte.hasNext()) {
			int possibleMoves = validationSetIte.next().intValue();
			String[] labels = new String[possibleMoves];
			double[] values = new double[possibleMoves];
			double sum = 0;
			
			for(int i = 0; i < possibleMoves; i++) {		
				labels[i] = moves.next();
				values[i] = ((Double) myStrategy.get(labels[i])).doubleValue();
				sum += values[i];
			}
			
			if(sum != 1) {
				
				// In the previous current Game's play,
				// I couldn't infer what the adversary played here
				// Will be applied a random move on this validation set
				sum = 0;
				
				for(int i = 0; i < values.length - 1; i++) {
					values[i] = random.nextDouble();
					while(sum + values[i] >= 1) values[i] = random.nextDouble();
					sum = sum + values[i];
				}
				
				values[values.length - 1] = ((double) 1) - sum;
				
				for(int i = 0; i < possibleMoves; i++) {
					myStrategy.put(labels[i], values[i]);
					System.err.println("Unexplored path: Setting " + labels[i] + " with probability of " + values[i]);
				}
			}	
		}
	}
	

	@Override
	public void execute() throws InterruptedException {

		SecureRandom random = new SecureRandom();

		while(!this.isTreeKnown()) {
			System.err.println("Waiting for the Game Tree to become available...");
			Thread.sleep(1000);
		}

		GameNode finalP1 = null;
		GameNode finalP2 = null;
				
		while(true) {

			PlayStrategy myStrategy = this.getStrategyRequest();
			
			// The current Game was terminated by an outside event
			if(myStrategy == null) {
				break;	
			}
			
			boolean playComplete = false;
			
			while(!playComplete) {
				if(myStrategy.getFinalP1Node() != -1) {
					finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					if(finalP1 != null)
						System.out.println("Final/Terminal node in last round as P1: " + finalP1);
				}

				if(myStrategy.getFinalP2Node() != -1) {
					finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if(finalP2 != null)
						System.out.println("Final/Terminal node in last round as P2: " + finalP2);
				}

				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = myStrategy.keyIterator();

				if(finalP1 == null || finalP2 == null) {
					
					// This is the first round, so, I will start to cooperate.
					while(iterator.hasNext()) {
						double[] moves = new double[iterator.next()];
						
						// Here, I will start to cooperate, as both, Player no. 1 and Player no. 2
						moves[0] = 1.0;
						moves[1] = 0.0;
						
						for(int i = 0; i < moves.length; i++) {
							
							if(!keys.hasNext()) {
								System.err.println("PANIC: Strategy structure doesn't match the current Game!!!");
								return;
							}
							
							String firstPlay = keys.next();

							System.out.println();
							System.out.println("My First Play - " + firstPlay + " with probability of " + moves[i]);
							
							myStrategy.put(firstPlay, moves[i]);
						}
					} 
				}
				else {
					
					// Let's, now, play the Gradual Strategy (at least what we can infer)
					List<GameNode> listP1 = getReversePath(finalP1);
					List<GameNode> listP2 = getReversePath(finalP2);
					
					try {
						cumputeStrategy(listP1, listP2, myStrategy, random);
					}
					catch (GameNodeDoesNotExistException gameNodeDoesNotExistException) {
						System.err.println("PANIC: Strategy structure doesn't match the current Game!!!");
					}
				}

				try {
					this.provideStrategy(myStrategy);
					playComplete = true;
				}
				catch(InvalidStrategyException invalidStrategyException) {
					System.err.println("Invalid Strategy: " + invalidStrategyException.getMessage());;
					invalidStrategyException.printStackTrace(System.err);
				} 
			}
		}
	}
}