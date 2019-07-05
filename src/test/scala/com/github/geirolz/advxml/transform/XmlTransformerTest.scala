package com.github.geirolz.advxml.transform

import org.scalatest.FeatureSpec

import scala.util.Try
import scala.xml.Elem

class XmlTransformerTest extends FeatureSpec  {

  import cats.instances.try_._
  import com.github.geirolz.advxml.transform.XmlTransformer.implicits._
  import com.github.geirolz.advxml.transform.XmlTransformer.instances._

  feature("Xml manipulation: Filters") {
    scenario("Filter By Attribute") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
          <OrderLine PrimeLineNo="3" />
        </OrderLines>
      </Order>

      val result = elem \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1")

      assert(result \@ "PrimeLineNo" == "1")
    }
  }

  feature("Xml manipulation: Nodes") {

    scenario("AppendNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

      val result = elem.transform(
          $(_ \ "OrderLines")
            ==> Append(<OrderLine PrimeLineNo="2" />)
            ==> Append(<OrderLine PrimeLineNo="3" />)
            ==> Append(<OrderLine PrimeLineNo="4" />)
      )

      assert(result.get \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "1"))
      assert(result.get \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "2"))
      assert(result.get \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "3"))
      assert(result.get \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "4"))
    }

    scenario("ReplaceNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

      val result = elem.transform[Try](
        $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1"))
          ==> Replace(<OrderLine PrimeLineNo="4" />)
      )

      assert((result.get \ "OrderLines" \ "OrderLine"
        filter attrs("PrimeLineNo" -> "1")).length == 0)
      assert(result.get \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "4"))
    }

    scenario("RemoveNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1")) ==> Remove
      )

      assert((result.get \ "OrderLines" \ "OrderLine"
        filter attrs("PrimeLineNo" -> "1")).length == 0)
    }

    scenario("RemoveNode root") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val result = elem.transform(Remove)

      assert(result.get.isEmpty)
    }

    scenario("AppendNode to Root"){
      val elem: Elem = <OrderLines />
      val result = elem.transform[Try](
        Append(<OrderLine PrimeLineNo="1" />)
      ).get

      assert((result \ "OrderLine").length == 1)
      assert(result \ "OrderLine" \@ "PrimeLineNo" == "1")
    }
  }

  feature("Xml manipulation: Attributes") {

    scenario("SetAttribute") {
      val elem: Elem = <Order><OrderLines /></Order>

      val result = elem.transform(
        $(_ \ "OrderLines") ==> SetAttrs("A1" -> "1", "A2" -> "2", "A3" -> "3")
      )

      assert(result.get \ "OrderLines" \@ "A1" == "1")
      assert(result.get \ "OrderLines" \@ "A2" == "2")
      assert(result.get \ "OrderLines" \@ "A3" == "3")
    }

    scenario("SetAttribute to root") {
      val elem: Elem = <Order />

      val result = elem.transform(
        SetAttrs("A1" -> "1", "A2" -> "2", "A3" -> "3")
      )

      assert(result.get \@ "A1" == "1")
      assert(result.get \@ "A2" == "2")
      assert(result.get \@ "A3" == "3")
    }


    scenario("ReplaceAttribute") {
      val elem: Elem = <Order>
        <OrderLines T1="1">
          <OrderLine PrimeLineNo="1"></OrderLine>
          <OrderLine PrimeLineNo="2"></OrderLine>
          <OrderLine PrimeLineNo="3"></OrderLine>
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines") ==> SetAttrs("T1" -> "EDITED")
      )

      assert(result.get \ "OrderLines" \@ "T1" == "EDITED")
    }

    scenario("RemoveAttribute") {
      val elem: Elem = <Order>
        <OrderLines T1="1">
          <OrderLine PrimeLineNo="1"></OrderLine>
          <OrderLine PrimeLineNo="2"></OrderLine>
          <OrderLine PrimeLineNo="3"></OrderLine>
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines") ==> RemoveAttrs("T1")
      )

      assert(result.get \ "OrderLines" \@ "T1" == "")
    }
  }
}
