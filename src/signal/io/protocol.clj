;; Copyright 2016-2018 Boundless, http://boundlessgeo.com
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LCENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS S" BASS,
;; WTHOUT WARRANTES OR CONDTONS OF ANY KND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns signal.io.protocol)

(defprotocol PollingInput
  (interval [this])
  (poll [this v]))

(defmulti make-polling-input :type)

(defprotocol StreamingInput
  (start-input [this func])
  (stop-input [this]))

(defmulti make-streaming-input :type)

(defprotocol StreamingOutput
  (start-output [this func])
  (stop-output [this]))

(defmulti make-streaming-output :type)

(defprotocol Output
  (recipients [this])
  (send! [this v]))

(defmulti make-output :type)
