package kweb.state

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kweb.state.ObservableList.Modification.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

class ObservableListSpec : FreeSpec({
    "ObservableList" - {
        "should be the correct size" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            list.size shouldBe 4
        }
        "should be the correct value" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            list shouldBe listOf(1, 2, 3, 4)
        }
        "should be the correct subList" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            list.subList(1, 3) shouldBe listOf(2, 3)
        }
        "should notify listener of element addition" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            val notified = AtomicBoolean(false)
            list.addListener { modifications ->
                modifications.forEach { modification ->
                    if (modification is Insertion) {
                        if (modification.position == 4 && modification.item == 99) {
                            notified.set(true)
                        }
                    }
                }
            }
            list.add(99)
            list.last() shouldBe 99

            eventually(1.seconds) {
                notified.get() shouldBe true
            }
        }

        "should notify listener of element deletion" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            val notified = AtomicBoolean(false)
            list.addListener { modifications ->
                modifications.forEach { modification ->
                    if (modification is ObservableList.Modification.Deletion) {
                        if (modification.position == 2 ) {
                            notified.set(true)
                        }
                    }
                }
            }
            list.remove(3)
            list shouldBe listOf(1, 2, 4)

            eventually(1.seconds) {
                notified.get() shouldBe true
            }
        }

        "should notify listener of element modification" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            val notified = AtomicBoolean(false)
            list.addListener { modifications ->
                modifications.forEach { modification ->
                    if (modification is Change) {
                        if (modification.position == 2 && modification.newItem == 99) {
                            notified.set(true)
                        }
                    }
                }
            }
            list[2] = 99
            list shouldBe listOf(1, 2, 99, 4)
            eventually(1.seconds) {
                notified.get() shouldBe true
            }
        }
        "should notify listener when element moved" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            val notified = AtomicBoolean(false)
            list.addListener { modifications ->
                modifications.forEach { modification ->
                    if (modification is Move) {
                        if (modification.oldPosition == 2 && modification.newPosition == 0) {
                            notified.set(true)
                        }
                    }
                }
            }
            list.move(2, 0)
            list shouldBe listOf(3, 1, 2, 4)
            eventually(1.seconds) {
                notified.get() shouldBe true
            }
        }
        "should call close listener when it's closed" {
            val list = ObservableList(listOf(1, 2, 3, 4))
            val notified = AtomicBoolean(false)
            list.addCloseListener {
                notified.set(true)
            }
            list.close()
            eventually(1.seconds) {
                notified.get() shouldBe true
            }
        }

        "listIterator" - {
            "should remove element correctly" {
                val list = ObservableList(listOf(1, 2, 3, 4))
                val listIterator = list.listIterator()
                listIterator.next() shouldBe 1
                listIterator.next() shouldBe 2
                listIterator.remove()
                list shouldBe listOf(1, 3, 4)
            }
            "should insert element correctly" {
                val list = ObservableList(listOf(1, 2, 3, 4))
                val listIterator = list.listIterator()
                listIterator.next() shouldBe 1
                listIterator.next() shouldBe 2
                listIterator.add(5)
                list shouldBe listOf(1, 2, 5, 3, 4)
            }
            "should set element correctly" {
                val list = ObservableList(listOf(1, 2, 3, 4))
                val listIterator = list.listIterator()
                listIterator.next() shouldBe 1
                listIterator.next() shouldBe 2
                listIterator.set(5)
                list shouldBe listOf(1, 5, 3, 4)
            }
        }
    }
})