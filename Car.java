/*
 * File name: Car.java
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

/**
 * Tracks the status of one car.
 *
 * @author Casey Cook
 *
 */
public class Car
{
	/**** All static variables and methods ****/
	static protected ExpDistribution carDistr; //Determines how cars arrive.
	static protected NormalDistribution serviceDistr; //Determines how cars request.
	static protected double currentTime, totalWaitingTime, mTime, totalServiceTime;
	static protected int totalCount = 0, totalEZPass;
	
	/**
	 * Sets the distribution of service time and traffic flow
	 * @param trafficFlow amount of cars per second
	 * @param meanServiceTime average time a car needs to be served
	 * @param var variance
	 */
	static public void setDistribution(double trafficFlow, double meanServiceTime, double var)
	{
		carDistr = new ExpDistribution(1/trafficFlow);
		serviceDistr = new NormalDistribution(meanServiceTime, var);
		totalCount = 0;
		currentTime = totalWaitingTime = 0;
		mTime = meanServiceTime;
	}
	
	/**
	 * Sets individual car attributes
	 * @return a car with arriving time and service time.
	 */
	static public Car next()
	{
		currentTime = currentTime + carDistr.next();
		double service = serviceDistr.sample();
		/*
		 * If the next service time is less than the mean time
		 *  then the service time is 1.0 s and it is an EZ-Pass car.
		 */
		if (service < mTime) 
		{
			service = 1.0;
			totalEZPass++;
		}
		//If service is not 1.0 seconds then the total service time gets accumulated.
		if(service != 1.0)
		{
			totalServiceTime += service;
		}
		totalCount++; //amount of cars goes up
		return new Car(currentTime, service);
	}
	
	/**
	 * @return total waiting time of all cars.
	 */
	public static double totalWaitingTime()
	{
		return totalWaitingTime;
	}
	
	/**
	 * @return total service time of the cars
	 * without EZ-Pass.
	 */
	public static double totalServiceTime()
	{
		return totalServiceTime;
	}
	
	/**
	 * @return total number of cars.
	 */
	public static int totalCount()
	{
		return totalCount;
	}
	/**
	 * @return total number of EZ-Pass cars.
	 */
	public static int totalEZPass()
	{
		return totalEZPass;
	}
	
	/**** All instance variables and methods ****/
	
	protected double timeToArrive, timeBeginsToServe, serviceNeeded;
	
	/**
	 * Constructor is not public.
	 * @param timeToArrive double time the car arrives to the toll lanes
	 * @param serviceNeeded double the amount of service time
	 */
	protected Car(double timeToArrive, double serviceNeeded)
	{
		this.timeToArrive = timeToArrive;
		this.serviceNeeded = serviceNeeded;
		timeBeginsToServe = -1;
	}
	
	/**
	 * @return the time this car will arrive.
	 */
	public double whenToArrive()
	{
		return timeToArrive;
	}
	
	/**
	 * @param time is the time this car begins to be served.
	 */
	public void setBeginToServe(double time)
	{
		timeBeginsToServe = time;
		totalWaitingTime += (this.timeBeginsToServe - this.timeToArrive); //Total waiting time accumulates.
	}
	
	/**
	 * @return compute when to leave after the service starts.
	 * @throws Exception
	 */
	public double whenToLeave()
	{
		if(timeBeginsToServe < 0)
		{
			System.out.println("Error");
			int a = 0;
			return 1E10/a;
		}
		return timeBeginsToServe + serviceNeeded;
	}
}