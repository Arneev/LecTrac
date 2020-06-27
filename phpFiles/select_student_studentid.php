<?php
//Constants
define("tblStudent",'STUDENT');
define("tblLecturer",'LECTURER');
define("tblWrote",'WROTE');
define("tblMessage",'MESSAGE');
define("tblWITS",'WITS');
define("tblCourse",'COURSE');
define("tblTest",'TEST');
define("tblTask",'TASK');
define("tblRegistered",'REGISTERED');
define("tblWITSCourse",'WITS_COURSE');
define("tblEnrolled",'ENROLLED');
define("tblWITSTest",'WITS_TEST');
//end constants

//standard connect
$username = "d2180393";
$password = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $password, $database);
$output=array();
//end standard connect

$table = $_REQUEST["table"];
$userID = $_REQUEST["userid"];
$sql = "SELECT * FROM ";

switch ($table) {
	case tblLecturer:
		break;
	case tblLecturer:
		break;
	case tblWrote:
		break;
	case tblMessage:
		break;
	case tblWITS:
		break;
	case tblCourse:
		break;
	case tblTest:
		break;
	case tblTask:
		break;
	case tblRegistered:
		break;
	case tblWITSCourse:
		break;
	case tblEnrolled:
		break;
	case tblWITSTest:
		break;
	default:
		echo json_encode($output);
		exit;
}

$sql .= $table;
$sql .= " WHERE User_ID = ?";

$stmt = $link->prepare($sql);
$stmt = $link->bind_param
$stmt->bind_param("s",$userID);

if ($result = $stmt->get_result()) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

$stmt->close();
$link->close();
echo json_encode($output);
?>