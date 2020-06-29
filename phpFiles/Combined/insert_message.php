<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$messageName = $_REQUEST["messageName"];
$messageClass = $_REQUEST["classification"];
$messageContents = $_REQUEST["contents"];
$messagePosted = $_REQUEST["datePosted"];
$messageCourse = $_REQUEST["courseCode"];
$lecturerID = $_REQUEST["userID"];

// "Sanitize" classification
switch ($messageClass) {
	case 'All':
		break;
	case 'Homework':
		break;
	case 'Anouncement':
		break;
	case 'Test':
		break;
	case 'Practice':
		break;
	default:
		$output[] = "Error";
		echo json_encode($output);
		exit;
}

// Course codes have a length of 8
if (strlen($messageCourse) != 8){
	$output[] = "Error";
	echo json_encode($output);
	exit;
}

// Dates have a length of 10
if (strlen($messagePosted) != 10){
	$output[] = "Error";
	echo json_encode($output);
	exit;
}


// length of lecturer ID must be 7
if (strlen($lecturerID) != 7){

	$output[] = "Error";
	echo json_encode($output);
	exit;
}

// Each character in lecturer ID must be a Number
for ($i = 0; $i < 7; $i++){

	if (!is_numeric($lecturerID[$i])){

		$output[] = "Error";
		echo json_encode($output);
		exit;
	}
}


$sql = "INSERT INTO MESSAGE (Message_Name, Message_Classification,
				Message_Contents, Message_Date_Posted, Course_Code, Lecturer_ID)
				VALUES(?,?,?,?,?,?)";


$stmt = $link->prepare($sql);

$stmt->bind_param("ssssss",$messageName,$messageClass,$messageContents
													,$messagePosted,$messageCourse,$lecturerID);


if ($stmt->execute()){
	$output = "Successful";
}else {
	$output = "Unsuccessful";
}

//standard connect

$stmt->close();
$link->close();
echo $output;
//end standard connect
?>
