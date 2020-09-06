<?php
$username = "d2180393";
$password = "6R7g5UVB";
$database = "dLecTrac";

$link = mysqli_connect("127.0.0.1", $username, $password, $database);

$query = $_REQUEST["query"];
$output=array();


if ($result = mysqli_query($link, $query)) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

mysqli_close($link);
echo json_encode($output);
?>
