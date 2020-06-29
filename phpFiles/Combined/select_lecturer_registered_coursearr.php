<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect
$userCourses = $_REQUEST["course"];

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

$size = count($userCourses);
$sql = "SELECT * FROM LECTURER,REGISTERED WHERE ";
$sql .= "(";

for ($i = 0; $i < $size; $i++){
		$sql .= "REGISTERED.Course_Code = '";
		$sql .= $userCourses[$i];
		$sql .= "'";
		if ($i != $size - 1){
				$sql .= " OR ";
		}
}

$sql .= ") AND LECTURER.Lecturer_ID = REGISTERED.Lecturer_ID";


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
