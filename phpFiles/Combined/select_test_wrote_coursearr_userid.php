<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect
$userCourses = $_REQUEST["course"];
$userID = $_REQUEST["userID"];

//start of course check
$coursesJSON = array();
$coursesJSON = include "select_coursecode_course.php";

$courses = json_decode($coursesJSON,true);
$temp = array();
foreach ($courses as $val) {
		foreach ($val as $value) {
				$temp[] = $value;
		}
}

$courses = $temp;

$totSize = count($courses);
$userSize = count($userCourses);


for($i = 0; $i < $userSize;  $i++){
			$isFound = false;
			for ($j = 0; $j < $totSize; $j++){
					if ($courses[$j] == $userCourses[$i]){
							$isFound = true;
							break;
					}
			}

			if (!$isFound){
				$output = array();
				$output[] = "Error";
				echo json_encode($output);
				exit;
			}
}

//end of course check

//start of userID check
$size = strlen($userID);
if ($size != 7){
    $output = array();
    $output[] = "Error";
    echo json_encode($output);
    exit;
}

try {
    $iUserID = (int)$userID;
} catch (Exception $e) {
  $output = array();
  $output[] = "Error";
  echo json_encode($output);
  exit;
}

//end of userID check

$size = count($userCourses);
$sql = "SELECT * FROM TEST,WROTE WHERE ";
$sql .= "(";

for ($i = 0; $i < $size; $i++){
		$sql .= "TEST.Course_Code = '";
		$sql .= $userCourses[$i];
		$sql .= "'";
		if ($i != $size - 1){
				$sql .= " OR ";
		}
}

$sql .= ") AND WROTE.Test_No = TEST.Test_No AND WROTE.Student_ID = '";
$sql .= $userID;
$sql .= "'";


//standard connect
$output = array();
$result = $link->query($sql);
if ($result) {
		while ($row=$result->fetch_assoc()){
				$output[]=$row;
		}
}

$link->close();
echo json_encode($output);
//end standard connect
?>
