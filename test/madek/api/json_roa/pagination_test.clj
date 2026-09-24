(ns madek.api.json-roa.pagination-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [madek.api.json-roa.collection-media-entry-arcs.core :as collection-media-entry-arcs]
   [madek.api.json-roa.collections :as collections]
   [madek.api.json-roa.group-users :as group-users]
   [madek.api.json-roa.groups :as groups]
   [madek.api.json-roa.people :as people]
   [madek.api.json-roa.roles :as roles]
   [madek.api.json-roa.users :as users]
   [madek.api.pagination :as pagination]))

(def ^:private base-request
  {:context "/api"
   :query-params {}})

(def ^:private paginated-resources
  [{:name "collections"
    :body-key :collections
    :build collections/index
    :request base-request}
   {:name "groups"
    :body-key :groups
    :build groups/groups
    :request base-request}
   {:name "group users"
    :body-key :users
    :build group-users/users
    :request (assoc base-request :route-params {:group-id "group-id"})}
   {:name "people"
    :body-key :people
    :build people/people
    :request base-request}
   {:name "roles"
    :body-key :roles
    :build roles/roles
    :request base-request}
   {:name "users"
    :body-key :users
    :build users/users
    :request base-request}
   {:name "collection media-entry arcs"
    :body-key :collection-media-entry-arcs
    :build collection-media-entry-arcs/index
    :request base-request}])

(defn- items [count]
  (mapv (fn [id] {:id (str id)}) (range count)))

(deftest exact-page-has-no-next-link
  (doseq [{:keys [name body-key build request]} paginated-resources]
    (testing name
      (let [response (pagination/paginated-response
                      {body-key (items pagination/LIMIT)})
            json-roa (build request response)]
        (is (= pagination/LIMIT
               (count (get-in response [:body body-key]))))
        (is (nil? (get-in json-roa [:collection :next])))))))

(deftest lookahead-row-adds-next-link-without-leaking
  (doseq [{:keys [name body-key build request]} paginated-resources]
    (testing name
      (let [response (pagination/paginated-response
                      {body-key (items (inc pagination/LIMIT))})
            json-roa (build request response)]
        (is (= pagination/LIMIT
               (count (get-in response [:body body-key]))))
        (is (some? (get-in json-roa [:collection :next])))))))

(deftest lookahead-query-fetches-one-extra-row
  (let [query (pagination/add-offset-with-lookahead-for-honeysql
               {}
               {:page 2})]
    (is (= (inc pagination/LIMIT) (:limit query)))
    (is (= (* 2 pagination/LIMIT) (:offset query)))))
