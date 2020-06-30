<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$taskName = $_REQUEST["taskName"];
$taskDate = $_REQUEST["dueDate"];
$taskTime = $_REQUEST["dueTime"];
$taskCourse = $_REQUEST["courseCode"];
$userID = $_REQUEST["userID"];

// Dates have a length of 10
if (strlen($taskDate) != 10){

	if ($taskDate != "NULL"){
		$output[] = "Error";
		echo json_encode($output);
		exit;
	}
}

// Course codes have a length of 8
if (strlen($taskCourse) != 8){
	$output[] = "Error";
	echo json_encode($output);
	exit;
}

// length must be 7
if (strlen($userID) != 7){

	$output[] = "Error";
	echo json_encode($output);
	exit;
}

// Each character must be a Number
for ($i = 0; $i < 7; $i++){

	if (!is_numeric($userID[$i])){

		$output[] = "Error";
		echo json_encode($output);
		exit;
	}
}



$sql = "INSERT INTO TASK (Task_Name, Task_Due_Date, Task_Due_Time, Course_Code, Lecturer_ID)
				VALUES(?,?,?,?,?)";


$stmt = $link->prepare($sql);
$stmt->bind_param("sssss",$taskName,$taskDate,$taskTime,$taskCourse,$userID);

if ($stmt->execute()){
	$output = "Successful";
}else {
	$output = "Unsuccessful";
}


//standard connect

$stmt->close();
$link->close();
echo json_encode($output);
//end standard connect
?>
