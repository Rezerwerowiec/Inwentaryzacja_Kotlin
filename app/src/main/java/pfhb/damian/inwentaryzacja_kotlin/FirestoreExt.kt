package pfhb.damian.inwentaryzacja_kotlin

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreExt {
    companion object{
val fs = FirestoreExt()

    }

    private lateinit var th : Thread
    private var counter = 0

    var result : Map<String, Any> = mapOf()
    var arrayResult : ArrayList<Map<String,Any>> = arrayListOf()


    private var database : FirebaseFirestore = FirebaseFirestore.getInstance()
    private fun getDataByDocumentIdMethod(dbName : String, docId : String, method: () -> Unit, onFailureMethod: (() -> Unit)?){
        Log.d(TAG, "RESULT: FireExt is called!")
        database.collection(dbName)
            .document(docId)
            .get()
            .addOnSuccessListener{
                if (it.data != null) {
                    Log.d(TAG, "RESULT: AddOnSuccess called...")
                    result = it.data!!
                    Log.d(TAG, "RESULT: GOT FROM FireExt! $result")
                    method()
                } else{
                    Log.d(TAG, "RESULT: IS NULL! FROM FireExt!")
                    if(onFailureMethod != null)
                        onFailureMethod()
                }
            }
            .addOnFailureListener { Log.d(TAG, "RESULT: AddOnFailure called...")
            }
            .addOnCanceledListener { Log.d(TAG, "RESULT: AddOnCancelled called....") }
        Log.d(TAG, "RESULT: FireExt before return")
    }
    private fun getDataByCollectionMethod(dbName : String, method: () -> Unit, onFailureMethod: (() -> Unit)?){
        Log.d(TAG, "RESULT: FireExt is called!")
        database.collection(dbName)
            .get()
            .addOnSuccessListener{
                for(doc in it) {
                    Log.d(TAG, "RESULT: AddOnSuccess called...")
                    var temp : Map<String, Any> = mapOf("docId" to doc.id)
                    temp = temp + doc.data
                    arrayResult.add( temp)
                    Log.d(TAG, "RESULT: GOT FROM FireExt! $arrayResult")
                    method()
                }
            }
            .addOnFailureListener { Log.d(TAG, "RESULT: AddOnFailure called...") }
            .addOnCanceledListener { Log.d(TAG, "RESULT: AddOnCancelled called....") }
        Log.d(TAG, "RESULT: FireExt before return")
    }

    fun getData(collection: String, documentId: String?, method: () -> Unit, onFailureMethod: (() -> Unit)?){
        clear()

//        th = Thread {
//            counter--
//            if (counter <= 0) {
//                if (documentId.isNullOrEmpty()) {
//                    getDataByCollectionMethod(collection, method)
//                    Log.d(TAG, "RESULT: $arrayResult :::in:test::: Counting... $counter")
//
//                } else {
//                    getDataByDocumentIdMethod(collection, documentId, method)
//                    Log.d(TAG, "RESULT: $result :::in:test::: Counting... $counter")
//
//                }
//                counter = 5
//            }
//            Thread.sleep(10000)
//
//            if (arrayResult.isNullOrEmpty()) getData(collection, documentId, method)
//        }
//
//        if(arrayResult.isNotEmpty()) return
//
//        th.start()

        if (documentId.isNullOrEmpty()) {
            getDataByCollectionMethod(collection, method, onFailureMethod)

        } else {
            getDataByDocumentIdMethod(collection, documentId, method, onFailureMethod)
        }
    }

    fun getData(collection: String, method : () -> Unit) {
        getData(collection, null, method, null)
    }
    fun getData(collection: String, method : () -> Unit, onFailureMethod : () -> Unit){
        getData(collection, null, method, onFailureMethod)
    }
    fun getData(collection: String, docId: String, method: () -> Unit){
        getData(collection, docId, method, null)
    }





    fun putData(collection: String, docId: String, data : HashMap<String, Any>, method: () -> Unit, onFailureMethod: (() -> Unit)?){

        database.collection(collection)
            .document(docId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "RESULT: PUT onSuccess called")

                method()
            }
            .addOnFailureListener {
                if (onFailureMethod != null) {
                    onFailureMethod()
                }
                Log.d(TAG, "RESULT: PUT onFailure called")
            }
    }

    fun putData(collection: String, docId: String, data: HashMap<String, Any>, method: () -> Unit){
        putData(collection, docId, data, method, null)
    }
    fun putListedData(collection: String, docId: String, data: HashMap<String, List<String>>, method: () -> Unit, onFailureMethod: () -> Unit){
        database.collection(collection)
            .document(docId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "RESULT: PUT onSuccess called")

                method()
            }
            .addOnFailureListener {
                if (onFailureMethod != null) {
                    onFailureMethod()
                }
                Log.d(TAG, "RESULT: PUT onFailure called")
            }
    }

    fun deleteData(collection: String, docId: String, method: () -> Unit, onFailureMethod: (() -> Unit)?){
        database.collection(collection)
            .document(docId)
            .delete()
            .addOnSuccessListener {
                method()
            }
            .addOnFailureListener {
                if (onFailureMethod != null) {
                    onFailureMethod()
                }
            }
    }

    fun clear(){
        result = mapOf()
        arrayResult = arrayListOf()
    }
}