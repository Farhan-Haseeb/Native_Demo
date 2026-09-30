// entry/src/main/ets/viewmodel/NavStackModel.ets
import { IDOBleDeviceModel, IDOTransType } from 'protocol_channel';
import { FunctionInfo } from './FunctionListViewModel';

/**
 * @desc Interface defining the navigation parameters, with all possible fields and their types.
 * All fields are optional (?) because not every navigation uses all of them.
 */
export interface NavParam {
  navTitle?: string | Resource;
  device?: IDOBleDeviceModel;
  autoConnect?:boolean,
  functionName?: string;
  initialParams?: string;
  funcInfo?: FunctionInfo;
  transType?: IDOTransType;
}

/**
 * @desc Data structure of each path node in the navigation stack.
 */
export class NavStackModel {
  // Unique name of the target
  name: string;
  // Parameters passed to the target view, typed as our explicitly defined NavParam interface
  param?: NavParam;

  constructor(name: string, param?: NavParam) {
    this.name = name;
    this.param = param;
  }
}