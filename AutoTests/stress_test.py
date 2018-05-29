import time
import unittest
from appium import webdriver
from random import randint

"""
TODO: Wait for sample app to complete in order to actually test
"""


class TestCases(unittest.TestCase):
    # vars
    myAddress = ''
    badAddress = ''
    noTrustAddress = 'GANFGTTCZL3D477BSCPR4RMUCX6RLERFUMOKZQYWK22ZFECZ3C7WXIZK'
    qaAccount = 'GBDUPSZP4APH3PNFIMYMTHIGCQQ2GKTPRBDTPCORALYRYJZJ35O2LOBL'

    # Desired Capabilities - Change this to whatever you are using
    appPackage = 'com.kik.atn.atnsample'
    appActivity = '.MainActivity'
    platformName = 'Android'
    platformVersion = '7.1.1'
    deviceName = 'ZX1G22BXGJ'
    server = 'http://127.0.0.1:4723/wd/hub'

    # Set up Appium and the app
    @classmethod
    def setUpClass(cls):
        # Sample App should be already installed on the emulator/device

        cls.driver = webdriver.Remote(
            command_executor=TestCases.server,  # Run on local server
            desired_capabilities={
                'appPackage': TestCases.appPackage,
                'appActivity': TestCases.appActivity,
                'platformName': TestCases.platformName,
                'platformVersion': TestCases.platformVersion,
                'deviceName': TestCases.deviceName
            }
        )
        cls._values = []
        # Timeout for element searching
        # Official documentation says its in millisecond, but it actually works in seconds for me
        cls.driver.implicitly_wait(10)

    # Called when the test run is over
    # @classmethod
    # def tearDownClass(cls):
    # Clear all data and close the app
    # os.system('adb shell pm clear' + TestCases.appPackage)

    def findById(self, id):
        return self.driver.find_element_by_id(TestCases.appPackage + ':id/' + id)

    def findByText(self, text):
        # yes, this is the right syntax
        return self.driver.find_element_by_android_uiautomator(
            'new UiSelector().text("{}")'.format(text))

    def test_1_CreateAccount(self):
        # Verify network buttons exist
        msgSentButton = self.findById('btnSent')
        msgReceivedButton = self.findById('btnReceived')

        msgSentButton.click()

        ##startTime = datetime.now()
        # while datetime.now() > (startTime + datetime.timedelta(min=1)):
        while True:
            randVal = randint(0, 1)
            if randVal == 0:
                msgReceivedButton.click()
            else:
                msgSentButton.click()
            time.sleep(5)


def main():
    suite = unittest.TestLoader().loadTestsFromTestCase(TestCases)
    unittest.TextTestRunner(verbosity=2).run(suite)


if __name__ == '__main__':
    main()
