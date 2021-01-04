const getCurrentDateAndTime = () => {
	// Date format produced => 27/12/2019/13:48:07/Mon
	// Get date of local country
	const dateString = new Date().toLocaleString('en-US', {timeZone: 'Asia/Karachi'});

	// Initialize Date object
	const date = new Date(dateString);

	// Get current year => (2019)
	let currentYear = date.getFullYear();
	// Get current month
	let currentMonth = date.getMonth() + 1;
	// Get current date
	let currentDate = date.getDate();
	// Get current day
	let currentDay = date.getDay();
	// Get current hours
	let currentHours = date.getHours();
	// Get current minutes
	let currentMins = date.getMinutes();
	// Get current seconds
	let currentSeconds = date.getSeconds();

	currentDate = addZero(currentDate);
	currentMonth = addZero(currentMonth);
	currentHours = addZero(currentHours);
	currentMins = addZero(currentMins);
	currentSeconds = addZero(currentSeconds);

	const currentDayOfWeek = dayOfWeek(currentDay);
	// Set the date in the format displayed above
	const currentDateAndTime = 
	currentDate 
	+ '/' 
	+ currentMonth 
	+ '/' 
	+ currentYear
	+ '/'
	+ currentHours
	+ ':'
	+ currentMins
	+ ':'
	+ currentSeconds
	+ '/'
	+ currentDayOfWeek;

	return currentDateAndTime;
}

// Convert day from numerical to name of the day
const dayOfWeek = (day) => {
	if(day === 1) return 'Mon';
	else if (day === 2) return 'Tue';
	else if (day === 3) return 'Wed';
	else if (day === 4) return 'Thu';
	else if (day === 5) return 'Fri';
	else if (day === 6) return 'Sat';
	else if (day === 0) return 'Sun';
	else return '';
}

// Preceed number with a zero
const addZero = (number) => {
	// If the number is less than 10, preceed the number with a zero
	if (number < 10) {
		number = '0' + number;
	}

	return number;
}

// Helper function to computer the age
const calculateAge = (dob) => {
	dob = dob.split('/');

	const dobDay = dob[0];
	const dobMonth = dob[1];
	const dobYear = dob[2];

	const date = new Date();
	const currentDate = date.getDate();
	const currentMonth = date.getMonth() + 1;
	const currentYear = date.getFullYear();

	// Convert date to age
	// Number of years
	const years = currentYear - dobYear;

	// Number of months
	let months = currentMonth - dobMonth;

	// Number of days (converting months to days and computing the age)
	let days = 0;

	// If months are negative then subtract months from 12 to indicate the total number of months
	// that have elapsed
	if (months < 0) months = 12 + months;

	let tempMonth = dobMonth;
	for (let i = 0; i < months; i++) {
		if (tempMonth === 13) tempMonth = 1;

		// Add the number of days according to the month
		if (tempMonth === 1) days = days + 31;
		else if (tempMonth === 2) days = days + 28;
		else if (tempMonth === 3) days = days + 31;
		else if (tempMonth === 4) days = days + 30;
		else if (tempMonth === 5) days = days + 31;
		else if (tempMonth === 6) days = days + 30;
		else if (tempMonth === 7) days = days + 31;
		else if (tempMonth === 8) days = days + 31;
		else if (tempMonth === 9) days = days + 30;
		else if (tempMonth === 10) days = days + 31;
		else if (tempMonth === 11) days = days + 30;
		else if (tempMonth === 12) days = days + 31;

		tempMonth++;
	}

	// Computer totals days by adding days of each month and 
	// then adding the day of the particular month
	days = days + (currentDate - dobDay);

	// Computer age in years
	const age = Math.floor(years + days / 365);
	return age;
}

module.exports = {
	getCurrentDateAndTime,
	calculateAge
}