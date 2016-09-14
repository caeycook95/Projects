/*
 * File name: Sim.java
 *
 * Programmer: Casey Cook
 * ULID: clcoo10
 *
 * Date: Oct 20, 2015
 *
 * Class: IT 179
 * Instructor: Dr. Li
 */
package myUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Simulates cars in a toll plaza.
 *
 * @author Casey Cook
 *
 */
public class Sim
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
		// Variables that are passed using String[] args
		double meanService = Double.parseDouble(args[0]); // Mean Service time - 3
		double varService = Double.parseDouble(args[1]); // Variance - 6 
		double trafficFlow = Double.parseDouble(args[2]); // Amount of cars/second - 1
		int boothNo = Integer.parseInt(args[3]); // Number of toll booths - 1
		
		//Simulated time: 3 hours
		double timeToSimulate = 3 * 3600;
		
		//Print out the beginning of the chart
		System.out.println("Simulation -- " + timeFormat(timeToSimulate) + " hours, Booth No:" + boothNo 
				+ ", Without EZ-Pass modification: m = " + meanService + " v = " + varService);
		System.out.println("** Flow:  " + trafficFlow + " cars/sec");
		
		//Run Simulation
		Simulate(trafficFlow, meanService, varService, timeToSimulate, boothNo);
		} 
		catch(Exception e)
		{
			System.out.println("Parameter Error:\nCheck: mean service, variance,"
					+ " traffic flow, and number of booths.");
			System.exit(-1);
		}
	}
	
	/**
	 * Simulates a toll plaza.
	 * @param trafficFlow amount of cars per second
	 * @param meanServiceTime average time a car needs served
	 * @param var variance
	 * @param timeToSimulate amount of time the simulation is going to run
	 * @param boothNo number of toll booths
	 */
	public static void Simulate(double trafficFlow, double meanServiceTime, double var, double timeToSimulate, int boothNo)
	{
		Car.setDistribution(trafficFlow, meanServiceTime, var);
		int maxNumberOfCarsWaiting = 0;
		//Holds all the cars that will go through the simulation.
		Queue<Car> carPool = new LinkedList<Car>();
		// The toll lane that has the car that is next to leave.
		Queue<Car> tollInUse = new LinkedList<Car>();
		//Cars that have arrived and have to wait for a toll lane to not be full.
		Queue<Car> waitingCars = new LinkedList<Car>(); 
		//All the toll lanes.
		ArrayList<Queue<Car>> tollBoothLanes = new ArrayList<Queue<Car>>(); 
		// Creates all the toll lanes depending on the number of booths passed into the method.
		for(int i = 0; i < boothNo; i++)
		{
			// Adds a queue to the ArrayList to simulate a toll booth.
			tollBoothLanes.add(new LinkedList<Car>());
		}
		
		int totalCount = 0; //total amount of cars that are served
		double clock = 0; // current time clock
		
		/*
		 * Creates all the car objects until the arrive time is over 
		 * the amount of time simulated and puts them into the pool of cars.
		 */
		Car car = Car.next();
		while(car.whenToArrive() <= timeToSimulate)
		{
			carPool.offer(car);
			car = Car.next();
		}
		totalCount = carPool.size();
		
		/* Begin While Loop*/
		// Loops until all cars are in toll both lanes.
		while(!carPool.isEmpty())
		{
			/*
			 * Updates the maximum number of cars waiting
			 *  to get into a toll lane each loop iteration.
			 */
			if(waitingCars.size() > maxNumberOfCarsWaiting)
			{maxNumberOfCarsWaiting = waitingCars.size();}
			
			/*
			 * Checks if all the toll booth lanes are empty and
			 * and puts a car in a random toll booth lane if all
			 * the lanes are empty.
			 */
			if(isTollBoothsEmpty(tollBoothLanes))
			{
				//Grabs the next car
				car = carPool.poll(); 
				//Updates the clock to the current time in the simulation.
				clock = car.whenToArrive();
				if(boothNo == 1)
				{
					tollBoothLanes.get(0).offer(car);
				}
				else
				{
					Random r = new Random();
					tollBoothLanes.get(r.nextInt(((boothNo) - 1) + 1) + 1 - 1).offer(car);
				}
				//Immediately serves the car since the lane is empty.
				car.setBeginToServe(clock); 
				continue; 
			}
			//Toll booth that has the next car to leave.
			tollInUse = nextToLeave(tollBoothLanes);
			
			/*
			 * Check if the next to leave in the toll booth leaves first or
			 *  if another car arrives to the tolls first.
			 */
			//Car arrives to the toll lines first.
			if(carPool.peek().whenToArrive() < tollInUse.peek().whenToLeave())
			{
				//Grabs next car
				car = carPool.poll();
				//Updates the clock to the current time in the simulation.
				clock = car.whenToArrive(); 
				//Gets the toll booth with the shortest line.
				tollInUse = shortestLine(tollBoothLanes);
				/*
				 * If all the toll lines are full then the car
				 * will be waiting to get into the toll lanes.
				 *  
				 */
				if(tollLineFull(tollBoothLanes))
				{
					/*
					 * Since the tolls are full the car has to wait for the tolls to be free.
					 * So the queue changes to the waiting cars queue
					 */
					tollInUse = waitingCars;
				}
				// Puts the car into a toll lane or the car has to wait.
				tollInUse.offer(car); 
				/*
				 * Serves the car just put into the lane right away
				 * if its at the front of the lane and its not in
				 * the line of waiting cars.
				 * 
				 */
				if(tollInUse.size() == 1 && !tollInUse.equals(waitingCars))
				{
					car.setBeginToServe(clock);
				}
				continue;
			}
			
			/*
			 * Front one in tollInUse finishes first.
			 */
			// The car being served leaves.
			car = tollInUse.poll();
			//Updates the clock to the current time in the simulation.
			clock = car.whenToLeave();
			//Begins to serve the next car in the toll if there is one.
			if(!tollInUse.isEmpty())
			{
				tollInUse.peek().setBeginToServe(clock);
			}
			// Puts a car that was waiting into the toll line.
			if(!waitingCars.isEmpty())
			{
				tollInUse.offer(waitingCars.poll());
			}
		}
		/* End While Loop*/
		
		/*
		 * Flush all cars in the lines
		 */
		//Toll booth that has the next car to leave.
		tollInUse = nextToLeave(tollBoothLanes);
		
		while(tollInUse != null)
		{
			// The car being served leaves.
			car = tollInUse.poll();
			//Updates the clock to the current time in the simulation.
			clock = car.whenToLeave();
			//Begins to serve the next car in the toll if there is one.
			if(!tollInUse.isEmpty())
			{
				tollInUse.peek().setBeginToServe(clock);
			}
			
			//Toll booth that has the next car to leave.
			tollInUse = nextToLeave(tollBoothLanes);
		}
		
		/*
		 * Calculates the average waiting time for a car to get through the toll lane.
		 */
		double averageWaiting = Car.totalWaitingTime()/totalCount;
		/*
		 * Calculates the average service time for the cars without an EZ-Pass.
		 */
		double averageServiceTime = Car.totalServiceTime()/(totalCount - Car.totalEZPass());
		
		// Prints the simulation data. 
		System.out.println("** Total cars:  " + totalCount);
		System.out.println("** EZ-Pass cars:  " + Car.totalEZPass());
		System.out.printf("** Average service time without EZ-Pass:  %1.3f secs%n",averageServiceTime);
		System.out.println("** Max number of cars waiting on the road:  " + maxNumberOfCarsWaiting);
		System.out.println("** Average waiting time:  " + timeFormat(averageWaiting));
		
	}
	// End simulate
	
	/**
	 * Which line has next car to leave.
	 * @return Queue<Car> the queue that has the next car to leave
	 */
	private static Queue<Car> nextToLeave(ArrayList<Queue<Car>> tollBoothLanes )
	{
		//Checks to see of all the toll booth lanes are empty
		if(isTollBoothsEmpty(tollBoothLanes)) return null;
		
		//Assumes the first toll lane is the next to leave
		Queue<Car> nextLeave = tollBoothLanes.get(0);
		
		/*
		 * Checks first if the nextLeave queue is empty
		 * because then it is not next to leave.
		 * Then the nextLeave queue variable is the next
		 *  non-empty queue.
		 */
		for(int i = 0; i < tollBoothLanes.size(); i++)
		{
			if(!tollBoothLanes.get(i).isEmpty())
			{
				nextLeave = tollBoothLanes.get(i);
				break;
			}
		}
		
		/*
		 * Cycle through all the toll booths to see which first car
		 *  in the each toll lane is next to leave
		 */
		for(int i = 1; i < tollBoothLanes.size(); i++)
		{
			if( !tollBoothLanes.get(i).isEmpty() && nextLeave.peek().whenToLeave()
				> tollBoothLanes.get(i).peek().whenToLeave())
			{
				nextLeave = tollBoothLanes.get(i);
			}
		}
		
		// Returns the car in all the toll lanes that is next to leave.
		return nextLeave;
	}
	
	/**
	 * 
	 * @param tollBoothLanes ArrayList<Queue<Car>> all the toll lanes
	 * @return true if all the toll booths are empty
	 */
	private static boolean isTollBoothsEmpty(ArrayList<Queue<Car>> tollBoothLanes)
	{
		boolean allEmpty = true;
		for(int i = 0; i < tollBoothLanes.size(); i++)
		{
			// if one lane has less than 21 cars then allEmpty variable changes to false
			if(!tollBoothLanes.get(i).isEmpty())
			{allEmpty = false;}
		}
		return allEmpty;
	}
	
	/**
	 * 
	 * @param tollBoothLanes ArrayList<Queue<Car>> all the toll lanes
	 * @return Queue<Car> the queue that has the least amount of cars
	 */
	private static Queue<Car> shortestLine(ArrayList<Queue<Car>> tollBoothLanes )
	{
		Queue<Car> shortestLine = tollBoothLanes.get(0);
		
		for(int i = 1; i < tollBoothLanes.size(); i++)
		{
			/*
			 * If the assumed shortest lane is bigger than the one
			 * being compared then the shortest lane becomes the one being compared
			 */
			if( shortestLine.size() > tollBoothLanes.get(i).size())
			{
				shortestLine = tollBoothLanes.get(i);
			}
		}
		
		return shortestLine;
	}
	
	/**
	 * 
	 * @param tollBoothLanes ArrayList<Queue<Car>> all the toll lanes
	 * @return true if all the lines have more than 20 cars in them
	 */
	private static boolean tollLineFull(ArrayList<Queue<Car>> tollBoothLanes)
	{
		boolean full = true;
		for(int i = 0; i < tollBoothLanes.size(); i++)
		{
			if(tollBoothLanes.get(i).size() <= 20)
			{
				full = false;
			}
		}
		
		return full;
	}
	
	/**
	 * 
	 * @param T double time in seconds
	 * @return String that has time formated as HH:MM:SS
	 */
	public static String timeFormat(double T)
	{
		String str = "";
		long s, m, t = Math.round(T) ;
		s = t % 60; 
		str = s + str;
		if(s < 10) str = "0" + str;
		t = t / 60;
		m = (t % 60);
		str = m + ":" + str;
		if ( m < 10) str = "0" + str;
		t = t/60;
		return t + ":" + str;
	}
}