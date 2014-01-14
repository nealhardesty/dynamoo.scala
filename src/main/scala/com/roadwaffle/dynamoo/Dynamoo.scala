package com.roadwaffle.dynamoo

import com.typesafe.config._
import java.io.File
import scala.collection.JavaConversions._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.route53._
import com.amazonaws.services.route53.model._

object Dynamoo {
	val config = ConfigFactory.parseFile(new File(System.getenv("HOME") + "/.dynamoo.config.json"))
	val accessKeyId = config.getString("accessKeyId");
	val secretAccessKey = config.getString("secretAccessKey");
	val region = config.getString("region");
	val domains = for (d <- config.getString("domains").split(",")) yield d.trim + "."
	val hostname = java.net.InetAddress.getLocalHost.getHostName
	val extrahosts: List[String] = if(config.hasPath("extrahosts")) for (d <- config.getString("extrahosts").split(",").toList) yield d.trim else List[String]()
	val cred = new BasicAWSCredentials(accessKeyId, secretAccessKey)
	val client = new AmazonRoute53Client(cred);


	def main(args: Array[String]) {
		val ip = getMyIp
		val hosts = List(hostname).union(extrahosts)
		println("dynamoo " + hostname + " " + ip) 
		for(zone <- getHostedZones) {
			for(host <- hosts) {
				val fullname = host + "." + zone.getName
				if(domains.contains(zone.getName)) {
					val rrset = getResourceRecordSets(zone.getId, fullname);
					if(rrset.isEmpty) {
						println("creating " + fullname)
						val changeRequest = makeCreateRecord(zone.getId, fullname, ip);
						val response = client.changeResourceRecordSets(changeRequest);
						println("create response: " + response);
					} else {
						if(needsUpdate(rrset(0), ip)) {
							println("updating " + fullname)
							val changeRequest = makeUpdateRecord(rrset(0), zone.getId, fullname, ip);
							val response = client.changeResourceRecordSets(changeRequest);
							println("create response: " + response)
						} else {
							println(fullname + " does not need update");
						}
					}
				}
			}
		}
	}

	def needsUpdate(rrset: ResourceRecordSet, ip: String): Boolean = {
		return !rrset.getResourceRecords.exists(_.getValue == ip);
	}

	def makeCreateRecord(zoneId: String, fullname: String, ip: String): ChangeResourceRecordSetsRequest = {
		val rrset = new ResourceRecordSet(fullname, RRType.A)
										.withTTL(60l)
										.withResourceRecords(List( new ResourceRecord(ip) ))
		val changes = List(new Change(ChangeAction.CREATE, rrset))
		val changeBatch = new ChangeBatch(changes)
		return new ChangeResourceRecordSetsRequest(zoneId, changeBatch)
	}

	def makeUpdateRecord(currentrrset: ResourceRecordSet, zoneId: String, fullname: String, ip: String): ChangeResourceRecordSetsRequest = {
		val rrset = new ResourceRecordSet(fullname, RRType.A)
										.withTTL(60l)
										.withResourceRecords(List( new ResourceRecord(ip) ))
		val changes = List(new Change(ChangeAction.DELETE, currentrrset), new Change(ChangeAction.CREATE, rrset))
		val changeBatch = new ChangeBatch(changes)
		return new ChangeResourceRecordSetsRequest(zoneId, changeBatch)
	}

	def getHostedZones(): List[HostedZone] = {
		return client.listHostedZones.getHostedZones.toList
	}

	def getResourceRecordSets(zoneId: String, host: String): List[ResourceRecordSet] = {
		return client.listResourceRecordSets(
			new ListResourceRecordSetsRequest(zoneId)
				.withMaxItems("1")
				.withStartRecordName(host))
			.getResourceRecordSets.toList.filter(rrset => rrset.getName == host)
	}

	private val ipRegex = "[^\\d\\.]".r

	def getMyIp(): String = {
		val line = scala.io.Source.fromURL("http://checkip.dyndns.org", "UTF-8").getLines().mkString("\n")
		return ipRegex.replaceAllIn(line, (m) => "")
	}


}
