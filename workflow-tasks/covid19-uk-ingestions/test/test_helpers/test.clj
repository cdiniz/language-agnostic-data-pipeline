(ns test-helpers.test
  (:require [clojure.test :refer [deftest]]
            [next.jdbc :as jdbc]
            [migrator.migrate :as migrator]
            [common.adapters.db]
            )
  (:import org.testcontainers.containers.PostgreSQLContainer))

(defonce ^:dynamic *db*
         (let [postgres (PostgreSQLContainer. "postgres")

               _ (.. postgres start)

               db {:dbtype   PostgreSQLContainer/NAME
                   :dbname   (.. postgres getDatabaseName)
                   :host     (.. postgres getContainerIpAddress)
                   :port     (.. postgres (getMappedPort PostgreSQLContainer/POSTGRESQL_PORT))
                   :user     (.. postgres getUsername)
                   :password (.. postgres getPassword)}]
           (migrator/perform-rollback db)
           (migrator/perform-migrate db)
           db))

(defn clean
  [f]
  (jdbc/with-transaction [db *db* {:rollback-only true}]
                         (binding [*db* db]
                           (f))))

(defmacro deftest-with-db
  [test-name & body]
  `(deftest ~test-name
     (clean (fn [] ~@body))))
