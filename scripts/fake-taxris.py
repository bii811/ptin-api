#!/usr/bin/env python3
"""Local stand-in for the TaxRIS mediation gateway, to exercise failure/retry paths that real TaxRIS cannot be
made to produce on demand. Used by bruno-e2e/tax-declaration-retry (local only, never UAT/prod).

  python3 scripts/fake-taxris.py [port]        # default 9099
  PTIN_MEDIATION_BASE_URL=http://localhost:9099 ./gradlew bootRun

issueIndividualTin / managePTinInformation: always issue a random 12-digit TIN.
createPfmFromCRSDtl (tax declaration), by invoice number (INV_NO):
  contains REJECT -> first call rejected (CD E01), later calls succeed
  contains HANG   -> first call sleeps past the client read timeout (10 s), later calls succeed
  otherwise       -> success
"""
import json, random, sys, time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

seen = {}


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        body = json.loads(self.rfile.read(int(self.headers.get("Content-Length", 0))) or "{}")
        if "issueIndividualTin" in self.path or "managePTinInformation" in self.path:
            out = {"ResTaxRIS": {"Result": {"CD": "000", "MSG": "Success", "CNT": "1"},
                                 "TinInfo": {"TIN": str(random.randrange(10**11, 10**12)),
                                             "TAXR_GV_NM": "E2e", "TAXR_FAM_NM": "Tester"}}}
        elif "createPfmFromCRSDtl" in self.path:
            inv = body.get("CrsPfms", {}).get("INV_NO", "")
            seen[inv] = seen.get(inv, 0) + 1
            first = seen[inv] == 1
            if "HANG" in inv and first:
                time.sleep(15)
            if "REJECT" in inv and first:
                out = {"Result": {"CD": "E01", "MSG": "Rejected by fake TaxRIS", "CNT": "0"}}
            else:
                out = {"Result": {"CD": "000", "MSG": "Success", "CNT": "1"}}
        else:
            self.send_error(404)
            return
        data = json.dumps(out).encode()
        try:
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
        except (BrokenPipeError, ConnectionResetError):
            pass

    def log_message(self, *a):
        pass


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 9099
    ThreadingHTTPServer(("127.0.0.1", port), Handler).serve_forever()
