<?php
$username = "s2180393";
$password = "Moh@nSingh9";
$database = "d2180393";

$link = mysqli_connect("127.0.0.1", $username, $password, $database);

$query = $_REQUEST["query"];
$output=array();


if ($result = mysqli_query($link, query)) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

mysqli_close($link);
echo json_encode($output);
?>
